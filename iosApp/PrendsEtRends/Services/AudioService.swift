import Foundation
import AVFoundation
import Combine

public final class AudioService: NSObject, ObservableObject, AVAudioPlayerDelegate, AVAudioRecorderDelegate {
    public static let shared = AudioService()
    
    private var audioRecorder: AVAudioRecorder?
    private var audioPlayer: AVAudioPlayer?
    
    @Published public var isRecording: Bool = false
    @Published public var isPlaying: Bool = false
    @Published public var currentPlayingPath: String?
    
    private var recordingURL: URL?
    
    public override init() {
        super.init()
    }
    
    public func requestMicrophonePermission() async -> Bool {
        await withCheckedContinuation { continuation in
            AVAudioSession.sharedInstance().requestRecordPermission { granted in
                continuation.resume(returning: granted)
            }
        }
    }
    
    public func startRecording() -> URL? {
        let session = AVAudioSession.sharedInstance()
        do {
            try session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker, .allowBluetooth])
            try session.setActive(true)
            
            let filename = "voice_note_\(UUID().uuidString).m4a"
            let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            let fileURL = docs.appendingPathComponent(filename)
            self.recordingURL = fileURL
            
            let settings: [String: Any] = [
                AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
                AVSampleRateKey: 44100.0,
                AVNumberOfChannelsKey: 1,
                AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
            ]
            
            audioRecorder = try AVAudioRecorder(url: fileURL, settings: settings)
            audioRecorder?.delegate = self
            audioRecorder?.record()
            
            self.isRecording = true
            return fileURL
        } catch {
            print("Failed to start audio recording: \(error)")
            self.isRecording = false
            return nil
        }
    }
    
    public func stopRecording() -> String? {
        guard isRecording else { return nil }
        audioRecorder?.stop()
        audioRecorder = nil
        isRecording = false
        return recordingURL?.path
    }
    
    public func playAudio(from path: String) {
        let fileURL = URL(fileURLWithPath: path)
        guard FileManager.default.fileExists(atPath: fileURL.path) else {
            print("Audio file does not exist: \(path)")
            return
        }
        
        if currentPlayingPath == path && !isPlaying {
            audioPlayer?.play()
            isPlaying = true
            return
        }
        
        stopAudio()
        
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playback, mode: .default)
            try session.setActive(true)
            
            audioPlayer = try AVAudioPlayer(contentsOf: fileURL)
            audioPlayer?.delegate = self
            audioPlayer?.play()
            
            currentPlayingPath = path
            isPlaying = true
        } catch {
            print("Failed to play audio: \(error)")
            isPlaying = false
            currentPlayingPath = nil
        }
    }
    
    public func pauseAudio() {
        if isPlaying {
            audioPlayer?.pause()
            isPlaying = false
        }
    }
    
    public func stopAudio() {
        audioPlayer?.stop()
        audioPlayer = nil
        isPlaying = false
        currentPlayingPath = nil
    }
    
    public func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        isPlaying = false
        currentPlayingPath = nil
    }
}
