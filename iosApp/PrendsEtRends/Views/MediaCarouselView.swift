import SwiftUI

public struct MediaCarouselView: View {
    let attachments: [MediaItem]
    var onRemove: ((String) -> Void)? = nil
    
    public init(attachments: [MediaItem], onRemove: ((String) -> Void)? = nil) {
        self.attachments = attachments
        self.onRemove = onRemove
    }
    
    public var body: some View {
        if !attachments.isEmpty {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(attachments) { item in
                        ZStack(alignment: .topTrailing) {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color(.secondarySystemBackground))
                                .frame(width: 80, height: 80)
                                .overlay(
                                    Image(systemName: item.type == .video ? "video.fill" : "photo")
                                        .font(.system(size: 24))
                                        .foregroundColor(.secondary)
                                )
                            
                            if let removeAction = onRemove {
                                Button(action: { removeAction(item.id) }) {
                                    Image(systemName: "xmark.circle.fill")
                                        .font(.system(size: 16))
                                        .foregroundColor(.gray)
                                        .background(Color.white.clipShape(Circle()))
                                }
                                .padding(4)
                            }
                        }
                    }
                }
            }
        }
    }
}
