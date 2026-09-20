import SwiftUI

public struct LoanCardView: View {
    let item: LoanItem
    let onToggleStatus: () -> Void
    let onDelete: () -> Void
    let onShareReminder: () -> Void
    
    public init(
        item: LoanItem,
        onToggleStatus: @escaping () -> Void,
        onDelete: @escaping () -> Void,
        onShareReminder: @escaping () -> Void
    ) {
        self.item = item
        self.onToggleStatus = onToggleStatus
        self.onDelete = onDelete
        self.onShareReminder = onShareReminder
    }
    
    private var isReturned: Bool {
        item.status == .returned
    }
    
    private var dateFormatter: DateFormatter {
        let df = DateFormatter()
        df.locale = Locale(identifier: "fr_FR")
        df.dateFormat = "dd MMM yyyy"
        return df
    }
    
    public var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            // Header: Type badge & Category chip
            HStack {
                // Type badge
                let isLent = item.type == .lent
                let prefix = isLent ? "Prêté à" : "Emprunté à"
                let badgeBg = isLent ? Color(red: 0.91, green: 0.96, blue: 0.91) : Color(red: 0.89, green: 0.95, blue: 0.99)
                let badgeText = isLent ? Color(red: 0.18, green: 0.49, blue: 0.20) : Color(red: 0.08, green: 0.40, blue: 0.75)
                
                HStack(spacing: 4) {
                    Image(systemName: isLent ? "arrow.up.right" : "arrow.down.left")
                        .font(.system(size: 11, weight: .bold))
                    Text("\(prefix) \(item.personName)")
                        .font(.system(size: 12, weight: .semibold))
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(badgeBg)
                .foregroundColor(badgeText)
                .cornerRadius(8)
                
                Spacer()
                
                // Category Chip
                HStack(spacing: 4) {
                    Image(systemName: item.category.systemIcon)
                        .font(.system(size: 11))
                    Text(item.category.label)
                        .font(.system(size: 11, weight: .medium))
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color(.secondarySystemBackground))
                .foregroundColor(.secondary)
                .cornerRadius(8)
            }
            
            // Title
            Text(item.title)
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(isReturned ? .secondary : .primary)
                .strikethrough(isReturned, color: .secondary)
            
            // Notes snippet
            if !item.notes.isEmpty {
                Text(item.notes)
                    .font(.system(size: 13))
                    .foregroundColor(.secondary)
                    .lineLimit(2)
            }
            
            // Due Date & Alert
            HStack(spacing: 6) {
                if let due = item.dueDate {
                    let date = Date(timeIntervalSince1970: TimeInterval(due / 1000))
                    let dateString = dateFormatter.string(from: date)
                    
                    if item.isOverdue {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.system(size: 12))
                            .foregroundColor(.red)
                        Text("En retard ! (devait être rendu le \(dateString))")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.red)
                    } else {
                        Image(systemName: "calendar")
                            .font(.system(size: 12))
                            .foregroundColor(.secondary)
                        let remaining = item.daysRemaining
                        let remainingText = (remaining != nil && remaining! >= 0) ? " (dans \(remaining!)j)" : ""
                        Text("À rendre pour le \(dateString)\(remainingText)")
                            .font(.system(size: 12))
                            .foregroundColor(.secondary)
                    }
                } else {
                    Image(systemName: "clock")
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                    Text("Sans date limite")
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                }
            }
            
            // Satellite Map Rendering (if location exists)
            if let lat = item.latitude, let lon = item.longitude {
                SatelliteMapView(
                    latitude: lat,
                    longitude: lon,
                    address: item.address,
                    personName: item.personName
                )
                .padding(.top, 4)
            }
            
            // Audio player widget
            if let audio = item.audioPath {
                AudioPlayerWidgetView(audioPath: audio)
                    .padding(.top, 4)
            }
            
            // Media attachments carousel
            if !item.mediaAttachments.isEmpty {
                MediaCarouselView(attachments: item.mediaAttachments)
                    .padding(.top, 4)
            }
            
            Divider().padding(.vertical, 2)
            
            // Actions
            HStack {
                Button(action: onToggleStatus) {
                    HStack(spacing: 5) {
                        Image(systemName: isReturned ? "arrow.uturn.backward" : "checkmark.circle.fill")
                            .font(.system(size: 13, weight: .semibold))
                        Text(isReturned ? "Remettre en cours" : "Marquer rendu ✓")
                            .font(.system(size: 13, weight: .semibold))
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(isReturned ? Color(.systemGray5) : Color.accentColor.opacity(0.15))
                    .foregroundColor(isReturned ? .primary : Color.accentColor)
                    .cornerRadius(8)
                }
                .buttonStyle(.plain)
                
                Spacer()
                
                if item.type == .lent && !isReturned {
                    Button(action: onShareReminder) {
                        Image(systemName: "square.and.arrow.up")
                            .font(.system(size: 14))
                            .foregroundColor(.secondary)
                            .padding(8)
                            .background(Color(.secondarySystemBackground))
                            .clipShape(Circle())
                    }
                    .buttonStyle(.plain)
                }
                
                Button(action: onDelete) {
                    Image(systemName: "trash")
                        .font(.system(size: 14))
                        .foregroundColor(.red.opacity(0.8))
                        .padding(8)
                        .background(Color.red.opacity(0.1))
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
            }
        }
        .padding(14)
        .background(Color(.systemBackground))
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(isReturned ? 0.02 : 0.05), radius: 6, x: 0, y: 2)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(.systemGray5), lineWidth: 1)
        )
    }
}
