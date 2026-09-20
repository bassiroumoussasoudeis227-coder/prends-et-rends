import Foundation

public final class FirebaseLoansService {
    public static let shared = FirebaseLoansService()
    
    // Firestore configuration
    private let projectId: String
    private let collectionName: String = "prends_et_rends_loans"
    private let session: URLSession
    
    public var isFirebaseAvailable: Bool {
        return !projectId.isEmpty
    }
    
    public init(projectId: String = "prends-et-rends-app", session: URLSession = .shared) {
        self.projectId = projectId
        self.session = session
    }
    
    private var baseFirestoreUrl: URL? {
        guard !projectId.isEmpty else { return nil }
        return URL(string: "https://firestore.googleapis.com/v1/projects/\(projectId)/databases/(default)/documents/\(collectionName)")
    }
    
    public func fetchAllLoans() async -> [LoanItem] {
        guard let url = baseFirestoreUrl else { return [] }
        do {
            let (data, response) = try await session.data(from: url)
            guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
                return []
            }
            
            if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
               let documents = json["documents"] as? [[String: Any]] {
                return documents.compactMap { doc -> LoanItem? in
                    guard let fields = doc["fields"] as? [String: Any] else { return nil }
                    return parseFirestoreFields(fields)
                }
            }
        } catch {
            print("Firebase fetch error: \(error.localizedDescription)")
        }
        return []
    }
    
    public func saveLoan(_ item: LoanItem) async -> Bool {
        guard let url = baseFirestoreUrl?.appendingPathComponent(item.id) else { return false }
        var request = URLRequest(url: url)
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "fields": [
                "id": ["stringValue": item.id],
                "title": ["stringValue": item.title],
                "personName": ["stringValue": item.personName],
                "type": ["stringValue": item.type.rawValue],
                "category": ["stringValue": item.category.rawValue],
                "startDate": ["integerValue": "\(item.startDate)"],
                "dueDate": item.dueDate != nil ? ["integerValue": "\(item.dueDate!)"] : ["nullValue": NSNull()],
                "notes": ["stringValue": item.notes],
                "status": ["stringValue": item.status.rawValue],
                "returnedDate": item.returnedDate != nil ? ["integerValue": "\(item.returnedDate!)"] : ["nullValue": NSNull()],
                "contactPhone": ["stringValue": item.contactPhone]
            ]
        ]
        
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: body)
            let (_, response) = try await session.data(for: request)
            if let httpResponse = response as? HTTPURLResponse {
                return (200...299).contains(httpResponse.statusCode)
            }
        } catch {
            print("Firebase save error: \(error.localizedDescription)")
        }
        return false
    }
    
    public func deleteLoan(id: String) async -> Bool {
        guard let url = baseFirestoreUrl?.appendingPathComponent(id) else { return false }
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        do {
            let (_, response) = try await session.data(for: request)
            if let httpResponse = response as? HTTPURLResponse {
                return (200...299).contains(httpResponse.statusCode)
            }
        } catch {
            print("Firebase delete error: \(error.localizedDescription)")
        }
        return false
    }
    
    private func parseFirestoreFields(_ fields: [String: Any]) -> LoanItem {
        let id = (fields["id"] as? [String: Any])?["stringValue"] as? String ?? UUID().uuidString
        let title = (fields["title"] as? [String: Any])?["stringValue"] as? String ?? ""
        let person = (fields["personName"] as? [String: Any])?["stringValue"] as? String ?? ""
        let rawType = (fields["type"] as? [String: Any])?["stringValue"] as? String ?? LoanType.lent.rawValue
        let rawCat = (fields["category"] as? [String: Any])?["stringValue"] as? String ?? LoanCategory.other.rawValue
        let rawStatus = (fields["status"] as? [String: Any])?["stringValue"] as? String ?? LoanStatus.active.rawValue
        let startStr = (fields["startDate"] as? [String: Any])?["integerValue"] as? String
        let dueStr = (fields["dueDate"] as? [String: Any])?["integerValue"] as? String
        let retStr = (fields["returnedDate"] as? [String: Any])?["integerValue"] as? String
        let notes = (fields["notes"] as? [String: Any])?["stringValue"] as? String ?? ""
        let phone = (fields["contactPhone"] as? [String: Any])?["stringValue"] as? String ?? ""
        
        return LoanItem(
            id: id,
            title: title,
            personName: person,
            type: LoanType(rawValue: rawType) ?? .lent,
            category: LoanCategory(rawValue: rawCat) ?? .other,
            startDate: Int64(startStr ?? "") ?? Int64(Date().timeIntervalSince1970 * 1000),
            dueDate: Int64(dueStr ?? ""),
            notes: notes,
            status: LoanStatus(rawValue: rawStatus) ?? .active,
            returnedDate: Int64(retStr ?? ""),
            contactPhone: phone
        )
    }
}
