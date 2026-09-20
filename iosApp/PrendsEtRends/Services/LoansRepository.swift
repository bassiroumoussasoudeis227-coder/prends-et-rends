import Foundation
import Combine

public final class LoansRepository: ObservableObject {
    public static let shared = LoansRepository()
    
    @Published public var loans: [LoanItem] = []
    @Published public var isSyncing: Bool = false
    
    private let firebaseService: FirebaseLoansService
    private let userDefaultsKey = "prends_et_rends_local_loans"
    
    public init(firebaseService: FirebaseLoansService = .shared) {
        self.firebaseService = firebaseService
        loadLocalLoans()
        if self.loans.isEmpty {
            self.loans = Self.getInitialSeedData()
            saveLocalLoans()
        }
        Task {
            await syncWithFirebase()
        }
    }
    
    @MainActor
    public func syncWithFirebase() async {
        isSyncing = true
        defer { isSyncing = false }
        
        let remoteLoans = await firebaseService.fetchAllLoans()
        if !remoteLoans.isEmpty {
            self.loans = remoteLoans
            saveLocalLoans()
        } else if !self.loans.isEmpty {
            // Seed remote with local items
            for loan in self.loans {
                _ = await firebaseService.saveLoan(loan)
            }
        }
    }
    
    @MainActor
    public func addLoan(_ loan: LoanItem) {
        loans.insert(loan, at: 0)
        saveLocalLoans()
        Task {
            _ = await firebaseService.saveLoan(loan)
        }
    }
    
    @MainActor
    public func updateLoan(_ loan: LoanItem) {
        if let index = loans.firstIndex(where: { it in it.id == loan.id }) {
            loans[index] = loan
            saveLocalLoans()
            Task {
                _ = await firebaseService.saveLoan(loan)
            }
        }
    }
    
    @MainActor
    public func toggleStatus(id: String) {
        guard let index = loans.firstIndex(where: { $0.id == id }) else { return }
        var item = loans[index]
        if item.status == .active {
            item.status = .returned
            item.returnedDate = Int64(Date().timeIntervalSince1970 * 1000)
        } else {
            item.status = .active
            item.returnedDate = nil
        }
        loans[index] = item
        saveLocalLoans()
        Task {
            _ = await firebaseService.saveLoan(item)
        }
    }
    
    @MainActor
    public func deleteLoan(id: String) {
        loans.removeAll { $0.id == id }
        saveLocalLoans()
        Task {
            _ = await firebaseService.deleteLoan(id: id)
        }
    }
    
    private func saveLocalLoans() {
        if let data = try? JSONEncoder().encode(loans) {
            UserDefaults.standard.set(data, forKey: userDefaultsKey)
        }
    }
    
    private func loadLocalLoans() {
        if let data = UserDefaults.standard.data(forKey: userDefaultsKey),
           let decoded = try? JSONDecoder().decode([LoanItem].self, from: data) {
            self.loans = decoded
        }
    }
    
    public static func getInitialSeedData() -> [LoanItem] {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let oneDay: Int64 = 24 * 60 * 60 * 1000
        return [
            LoanItem(
                id: "loan-1",
                title: "Perceuse à percussion Bosch",
                personName: "Thomas Dubois",
                type: .lent,
                category: .tools,
                startDate: now - 5 * oneDay,
                dueDate: now + 4 * oneDay,
                notes: "Avec le jeu de mèches à béton",
                status: .active
            ),
            LoanItem(
                id: "loan-2",
                title: "Livre Clean Architecture",
                personName: "Sophie Martin",
                type: .borrowed,
                category: .books,
                startDate: now - 10 * oneDay,
                dueDate: now + 2 * oneDay,
                notes: "À rendre après le chapitre 7",
                status: .active
            ),
            LoanItem(
                id: "loan-3",
                title: "Appareil à raclette 8 pers.",
                personName: "Camille Laurent",
                type: .borrowed,
                category: .other,
                startDate: now - 12 * oneDay,
                dueDate: now - 2 * oneDay, // overdue!
                notes: "Soirée chez nous le week-end dernier",
                status: .active
            ),
            LoanItem(
                id: "loan-4",
                title: "Manette Switch Pro",
                personName: "Marc Lefevre",
                type: .lent,
                category: .games,
                startDate: now - 20 * oneDay,
                dueDate: now - 5 * oneDay,
                notes: "Pour tester Mario Wonder",
                status: .returned,
                returnedDate: now - 4 * oneDay
            ),
            LoanItem(
                id: "loan-5",
                title: "Enceinte Bluetooth JBL",
                personName: "Alexandre Petit",
                type: .lent,
                category: .highTech,
                startDate: now - 2 * oneDay,
                dueDate: now + 10 * oneDay,
                notes: "Pour son barbecue",
                status: .active
            )
        ]
    }
}
