import SwiftUI
import CoreLocation

public struct AddLoanSheet: View {
    @Environment(\.dismiss) private var dismiss
    
    let onSave: (
        _ title: String,
        _ person: String,
        _ type: LoanType,
        _ category: LoanCategory,
        _ dueDate: Int64?,
        _ notes: String,
        _ latitude: Double?,
        _ longitude: Double?,
        _ address: String?,
        _ audioPath: String?,
        _ attachments: [MediaItem]
    ) -> Void
    
    @State private var type: LoanType = .lent
    @State private var title: String = ""
    @State private var personName: String = ""
    @State private var category: LoanCategory = .tools
    @State private var notes: String = ""
    @State private var daysAhead: Int? = 7
    
    // GPS state
    @StateObject private var locationService = LocationService.shared
    @State private var capturedLatitude: Double? = nil
    @State private var capturedLongitude: Double? = nil
    @State private var capturedAddress: String? = nil
    @State private var isCapturingLocation: Bool = false
    
    // Audio note state
    @StateObject private var audioService = AudioService.shared
    @State private var recordedAudioPath: String? = nil
    
    // Media attachments
    @State private var mediaAttachments: [MediaItem] = []
    
    public init(
        onSave: @escaping (
            _ title: String,
            _ person: String,
            _ type: LoanType,
            _ category: LoanCategory,
            _ dueDate: Int64?,
            _ notes: String,
            _ latitude: Double?,
            _ longitude: Double?,
            _ address: String?,
            _ audioPath: String?,
            _ attachments: [MediaItem]
        ) -> Void
    ) {
        self.onSave = onSave
    }
    
    private var isValid: Bool {
        !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        !personName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
    
    public var body: some View {
        NavigationView {
            Form {
                // Section Type
                Section {
                    Picker("Type d'opération", selection: $type) {
                        Text("Je prête").tag(LoanType.lent)
                        Text("J'emprunte").tag(LoanType.borrowed)
                    }
                    .pickerStyle(.segmented)
                }
                
                // Section Details
                Section(header: Text("Détails de l'objet")) {
                    TextField("Nom de l'objet (ex: Perceuse, Livre, 50€)", text: $title)
                    TextField(type == .lent ? "Prêté à qui ? (ex: Thomas)" : "Emprunté à qui ? (ex: Sophie)", text: $personName)
                }
                
                // Section Category
                Section(header: Text("Catégorie")) {
                    Picker("Catégorie", selection: $category) {
                        ForEach(LoanCategory.allCases, id: \.self) { cat in
                            Label(cat.label, systemImage: cat.systemIcon).tag(cat)
                        }
                    }
                }
                
                // Section Due Date
                Section(header: Text("Échéance de retour")) {
                    Picker("Délai", selection: $daysAhead) {
                        Text("Dans 7 jours").tag(Optional(7))
                        Text("Dans 14 jours").tag(Optional(14))
                        Text("Dans 1 mois").tag(Optional(30))
                        Text("Sans date limite").tag(Int?.none)
                    }
                }
                
                // Section SATELLITE GPS TRACKING
                Section(header: Text("Position Satellite GPS")) {
                    if let lat = capturedLatitude, let lon = capturedLongitude {
                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Image(systemName: "checkmark.circle.fill")
                                    .foregroundColor(.green)
                                Text(capturedAddress ?? "Position GPS capturée")
                                    .font(.system(size: 13, weight: .semibold))
                                Spacer()
                                Button("Effacer") {
                                    capturedLatitude = nil
                                    capturedLongitude = nil
                                    capturedAddress = nil
                                }
                                .font(.system(size: 12))
                                .foregroundColor(.red)
                            }
                            Text("Coordonnées: \(String(format: "%.4f", lat))°N, \(String(format: "%.4f", lon))°E")
                                .font(.system(size: 11))
                                .foregroundColor(.secondary)
                        }
                    } else {
                        Button(action: {
                            Task {
                                isCapturingLocation = true
                                do {
                                    let (loc, addr) = try await locationService.captureLocation()
                                    capturedLatitude = loc.coordinate.latitude
                                    capturedLongitude = loc.coordinate.longitude
                                    capturedAddress = addr
                                } catch {
                                    print("GPS capture error: \(error)")
                                }
                                isCapturingLocation = false
                            }
                        }) {
                            HStack {
                                Image(systemName: "location.fill")
                                    .foregroundColor(.blue)
                                Text(isCapturingLocation ? "Capture GPS en cours..." : "Capturer la position GPS actuelle")
                                    .font(.system(size: 14, weight: .medium))
                                if isCapturingLocation {
                                    Spacer()
                                    ProgressView()
                                }
                            }
                        }
                    }
                }
                
                // Section Audio Memo
                Section(header: Text("Mémo vocal")) {
                    if let audioPath = recordedAudioPath {
                        AudioPlayerWidgetView(audioPath: audioPath) {
                            recordedAudioPath = nil
                        }
                    } else {
                        Button(action: {
                            if audioService.isRecording {
                                recordedAudioPath = audioService.stopRecording()
                            } else {
                                Task {
                                    let granted = await audioService.requestMicrophonePermission()
                                    if granted {
                                        _ = audioService.startRecording()
                                    }
                                }
                            }
                        }) {
                            HStack {
                                Image(systemName: audioService.isRecording ? "stop.circle.fill" : "mic.circle.fill")
                                    .foregroundColor(audioService.isRecording ? .red : .blue)
                                Text(audioService.isRecording ? "Arrêter l'enregistrement" : "Enregistrer un mémo vocal")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(audioService.isRecording ? .red : .primary)
                            }
                        }
                    }
                }
                
                // Section Media Attachments
                Section(header: Text("Photos & Vidéos jointes")) {
                    MediaCarouselView(attachments: mediaAttachments) { id in
                        mediaAttachments.removeAll { $0.id == id }
                    }
                    
                    Button(action: {
                        let samplePhoto = MediaItem(uri: "sample_\(UUID().uuidString)", type: .photo)
                        mediaAttachments.append(samplePhoto)
                    }) {
                        HStack {
                            Image(systemName: "photo.badge.plus")
                            Text("Ajouter une photo / preuve")
                                .font(.system(size: 14))
                        }
                    }
                }
                
                // Section Notes
                Section(header: Text("Remarques / État (optionnel)")) {
                    TextField("Accessoires inclus, lieu de remise, etc.", text: $notes)
                }
            }
            .navigationTitle("Nouveau prêt / emprunt")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Annuler") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Enregistrer") {
                        let computedDue: Int64?
                        if let days = daysAhead {
                            computedDue = Int64(Date().addingTimeInterval(TimeInterval(days * 24 * 3600)).timeIntervalSince1970 * 1000)
                        } else {
                            computedDue = nil
                        }
                        onSave(
                            title,
                            personName,
                            type,
                            category,
                            computedDue,
                            notes,
                            capturedLatitude,
                            capturedLongitude,
                            capturedAddress,
                            recordedAudioPath,
                            mediaAttachments
                        )
                        dismiss()
                    }
                    .disabled(!isValid)
                    .fontWeight(.bold)
                }
            }
        }
    }
}
