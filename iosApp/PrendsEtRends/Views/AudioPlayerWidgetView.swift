import SwiftUI

public struct AudioPlayerWidgetView: View {
    let audioPath: String
    @ObservedObject var audioService = AudioService.shared
    var onDelete: (() -> Void)? = nil
    
    public init(audioPath: String, onDelete: (() -> Void)? = nil) {
        self.audioPath = audioPath
        self.onDelete = onDelete
    }
    
    private var isPlayingThis: Bool {
        audioService.isPlaying && audioService.currentPlayingPath == audioPath
    }
    
    public var body: some View {
        HStack {
            Button(action: {
                if isPlayingThis {
                    audioService.pauseAudio()
                } else {
                    audioService.playAudio(from: audioPath)
                }
            }) {
                Image(systemName: isPlayingThis ? "pause.circle.fill" : "play.circle.fill")
                    .font(.system(size: 28))
                    .foregroundColor(.blue)
            }
            
            VStack(alignment: .leading, spacing: 2) {
                Text("Mémo vocal")
                    .font(.system(size: 12, weight: .bold))
                Text(isPlayingThis ? "Lecture en cours..." : "Enregistrement audio")
                    .font(.system(size: 10))
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            if let del = onDelete {
                Button(action: del) {
                    Image(systemName: "trash")
                        .font(.system(size: 14))
                        .foregroundColor(.red)
                }
            }
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(10)
    }
}
