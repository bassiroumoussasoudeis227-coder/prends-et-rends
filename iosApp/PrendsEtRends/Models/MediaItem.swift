import Foundation

public enum MediaType: String, Codable {
    case photo = "PHOTO"
    case video = "VIDEO"
}

public struct MediaItem: Identifiable, Codable, Equatable {
    public var id: String
    public var uri: String
    public var type: MediaType
    public var thumbnailUri: String?
    
    public init(
        id: String = UUID().uuidString,
        uri: String,
        type: MediaType = .photo,
        thumbnailUri: String? = nil
    ) {
        self.id = id
        self.uri = uri
        self.type = type
        self.thumbnailUri = thumbnailUri
    }
    
    public func toDictionary() -> [String: Any] {
        var dict: [String: Any] = [
            "id": id,
            "uri": uri,
            "type": type.rawValue
        ]
        if let thumb = thumbnailUri { dict["thumbnailUri"] = thumb }
        return dict
    }
    
    public static func fromDictionary(_ dict: [String: Any]) -> MediaItem {
        let id = dict["id"] as? String ?? UUID().uuidString
        let uri = dict["uri"] as? String ?? ""
        let rawType = dict["type"] as? String ?? MediaType.photo.rawValue
        let type = MediaType(rawValue: rawType) ?? .photo
        let thumb = dict["thumbnailUri"] as? String
        return MediaItem(id: id, uri: uri, type: type, thumbnailUri: thumb)
    }
}
