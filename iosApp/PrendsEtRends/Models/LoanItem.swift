import Foundation

public enum LoanType: String, Codable, CaseIterable {
    case lent = "LENT"
    case borrowed = "BORROWED"
    
    public var label: String {
        switch self {
        case .lent: return "Je prête"
        case .borrowed: return "J'emprunte"
        }
    }
}

public enum LoanCategory: String, Codable, CaseIterable {
    case tools = "TOOLS"
    case books = "BOOKS"
    case highTech = "HIGH_TECH"
    case money = "MONEY"
    case games = "GAMES"
    case clothes = "CLOTHES"
    case other = "OTHER"
    
    public var label: String {
        switch self {
        case .tools: return "Outils"
        case .books: return "Livres"
        case .highTech: return "High-Tech"
        case .money: return "Argent"
        case .games: return "Jeux"
        case .clothes: return "Vêtements"
        case .other: return "Autre"
        }
    }
    
    public var systemIcon: String {
        switch self {
        case .tools: return "wrench.and.screwdriver"
        case .books: return "book.closed"
        case .highTech: return "laptopcomputer"
        case .money: return "banknote"
        case .games: return "gamecontroller"
        case .clothes: return "tshirt"
        case .other: return "shippingbox"
        }
    }
}

public enum LoanStatus: String, Codable, CaseIterable {
    case active = "ACTIVE"
    case returned = "RETURNED"
    
    public var label: String {
        switch self {
        case .active: return "En cours"
        case .returned: return "Rendu"
        }
    }
}

public struct LoanItem: Identifiable, Codable, Equatable {
    public var id: String
    public var title: String
    public var personName: String
    public var type: LoanType
    public var category: LoanCategory
    public var startDate: Int64
    public var dueDate: Int64?
    public var notes: String
    public var status: LoanStatus
    public var returnedDate: Int64?
    public var contactPhone: String
    // Satellite Location Tracking
    public var latitude: Double?
    public var longitude: Double?
    public var address: String?
    // Audio note
    public var audioPath: String?
    public var audioDurationSeconds: Int
    // Media attachments (photos & videos)
    public var mediaAttachments: [MediaItem]
    
    public init(
        id: String = UUID().uuidString,
        title: String,
        personName: String,
        type: LoanType = .lent,
        category: LoanCategory = .other,
        startDate: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        dueDate: Int64? = nil,
        notes: String = "",
        status: LoanStatus = .active,
        returnedDate: Int64? = nil,
        contactPhone: String = "",
        latitude: Double? = nil,
        longitude: Double? = nil,
        address: String? = nil,
        audioPath: String? = nil,
        audioDurationSeconds: Int = 0,
        mediaAttachments: [MediaItem] = []
    ) {
        self.id = id
        self.title = title
        self.personName = personName
        self.type = type
        self.category = category
        self.startDate = startDate
        self.dueDate = dueDate
        self.notes = notes
        self.status = status
        self.returnedDate = returnedDate
        self.contactPhone = contactPhone
        self.latitude = latitude
        self.longitude = longitude
        self.address = address
        self.audioPath = audioPath
        self.audioDurationSeconds = audioDurationSeconds
        self.mediaAttachments = mediaAttachments
    }
    
    public var isOverdue: Bool {
        guard status == .active, let due = dueDate else { return false }
        return due < Int64(Date().timeIntervalSince1970 * 1000)
    }
    
    public var daysRemaining: Int? {
        guard let due = dueDate else { return nil }
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let diffMs = due - now
        return Int(diffMs / (1000 * 60 * 60 * 24))
    }
    
    public var hasCoordinates: Bool {
        latitude != nil && longitude != nil
    }
    
    public func toFirestoreDictionary() -> [String: Any] {
        var dict: [String: Any] = [
            "id": id,
            "title": title,
            "personName": personName,
            "type": type.rawValue,
            "category": category.rawValue,
            "startDate": startDate,
            "notes": notes,
            "status": status.rawValue,
            "contactPhone": contactPhone,
            "audioDurationSeconds": audioDurationSeconds,
            "mediaAttachments": mediaAttachments.map { $0.toDictionary() }
        ]
        if let due = dueDate { dict["dueDate"] = due }
        if let ret = returnedDate { dict["returnedDate"] = ret }
        if let lat = latitude { dict["latitude"] = lat }
        if let lon = longitude { dict["longitude"] = lon }
        if let addr = address { dict["address"] = addr }
        if let audio = audioPath { dict["audioPath"] = audio }
        return dict
    }
    
    public static func fromFirestoreDictionary(_ dict: [String: Any]) -> LoanItem {
        let id = dict["id"] as? String ?? UUID().uuidString
        let title = dict["title"] as? String ?? ""
        let person = dict["personName"] as? String ?? ""
        let rawType = dict["type"] as? String ?? LoanType.lent.rawValue
        let rawCat = dict["category"] as? String ?? LoanCategory.other.rawValue
        let rawStat = dict["status"] as? String ?? LoanStatus.active.rawValue
        let type = LoanType(rawValue: rawType) ?? .lent
        let cat = LoanCategory(rawValue: rawCat) ?? .other
        let stat = LoanStatus(rawValue: rawStat) ?? .active
        let start = (dict["startDate"] as? NSNumber)?.int64Value ?? Int64(Date().timeIntervalSince1970 * 1000)
        let due = (dict["dueDate"] as? NSNumber)?.int64Value
        let ret = (dict["returnedDate"] as? NSNumber)?.int64Value
        let notes = dict["notes"] as? String ?? ""
        let phone = dict["contactPhone"] as? String ?? ""
        let lat = (dict["latitude"] as? NSNumber)?.doubleValue
        let lon = (dict["longitude"] as? NSNumber)?.doubleValue
        let addr = dict["address"] as? String
        let audio = dict["audioPath"] as? String
        let duration = (dict["audioDurationSeconds"] as? NSNumber)?.intValue ?? 0
        let rawMedia = dict["mediaAttachments"] as? [[String: Any]] ?? []
        let media = rawMedia.map { MediaItem.fromDictionary($0) }
        
        return LoanItem(
            id: id,
            title: title,
            personName: person,
            type: type,
            category: cat,
            startDate: start,
            dueDate: due,
            notes: notes,
            status: stat,
            returnedDate: ret,
            contactPhone: phone,
            latitude: lat,
            longitude: lon,
            address: addr,
            audioPath: audio,
            audioDurationSeconds: duration,
            mediaAttachments: media
        )
    }
}
