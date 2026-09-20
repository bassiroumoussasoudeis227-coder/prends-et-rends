import Foundation
import Combine

public enum FilterTab: String, CaseIterable, Identifiable {
    case all = "Tous"
    case lent = "Je prête"
    case borrowed = "J'emprunte"
    case overdue = "En retard"
    case returned = "Rendus"
    
    public var id: String { rawValue }
}

public final class LoansViewModel: ObservableObject {
    @Published public var searchQuery: String = ""
    @Published public var selectedFilter: FilterTab = .all
    @Published public var selectedCategory: LoanCategory? = nil
    
    @Published public private(set) var filteredItems: [LoanItem] = []
    @Published public private(set) var totalLent: Int = 0
    @Published public private(set) var totalBorrowed: Int = 0
    @Published public private(set) var totalOverdue: Int = 0
    @Published public private(set) var totalReturned: Int = 0
    @Published public private(set) var isSyncing: Bool = false
    
    private let repository: LoansRepository
    private var cancellables = Set<AnyCancellable>()
    
    public init(repository: LoansRepository = .shared) {
        self.repository = repository
        
        Publishers.CombineLatest4(
            repository.$loans,
            $searchQuery,
            $selectedFilter,
            $selectedCategory
        )
        .combineLatest(repository.$isSyncing)
        .receive(on: DispatchQueue.main)
        .sink { [weak self] (combined, syncing) in
            let (loans, query, filter, category) = combined
            self?.updateState(loans: loans, query: query, filter: filter, category: category, syncing: syncing)
        }
        .store(in: &cancellables)
    }
    
    private func updateState(
        loans: [LoanItem],
        query: String,
        filter: FilterTab,
        category: LoanCategory?,
        syncing: Bool
    ) {
        self.isSyncing = syncing
        self.totalLent = loans.filter { $0.type == .lent && $0.status == .active }.count
        self.totalBorrowed = loans.filter { $0.type == .borrowed && $0.status == .active }.count
        self.totalOverdue = loans.filter { $0.isOverdue }.count
        self.totalReturned = loans.filter { $0.status == .returned }.count
        
        let trimmedQuery = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        
        self.filteredItems = loans.filter { item in
            // Search match
            let matchesQuery: Bool
            if trimmedQuery.isEmpty {
                matchesQuery = true
            } else {
                matchesQuery = item.title.lowercased().contains(trimmedQuery) ||
                    item.personName.lowercased().contains(trimmedQuery) ||
                    item.notes.lowercased().contains(trimmedQuery) ||
                    (item.address?.lowercased().contains(trimmedQuery) ?? false)
            }
            
            // Category match
            let matchesCategory = (category == nil) || (item.category == category)
            
            // Filter match
            let matchesFilter: Bool
            switch filter {
            case .all:
                matchesFilter = true
            case .lent:
                matchesFilter = (item.type == .lent && item.status == .active)
            case .borrowed:
                matchesFilter = (item.type == .borrowed && item.status == .active)
            case .overdue:
                matchesFilter = item.isOverdue
            case .returned:
                matchesFilter = (item.status == .returned)
            }
            
            return matchesQuery && matchesCategory && matchesFilter
        }
    }
    
    public func toggleCategory(_ category: LoanCategory) {
        if selectedCategory == category {
            selectedCategory = nil
        } else {
            selectedCategory = category
        }
    }
    
    public func toggleStatus(for id: String) {
        repository.toggleStatus(id: id)
    }
    
    public func deleteLoan(id: String) {
        repository.deleteLoan(id: id)
    }
    
    public func saveLoan(
        title: String,
        personName: String,
        type: LoanType,
        category: LoanCategory,
        dueDate: Int64?,
        notes: String,
        latitude: Double? = nil,
        longitude: Double? = nil,
        address: String? = nil,
        audioPath: String? = nil,
        attachments: [MediaItem] = []
    ) {
        let newLoan = LoanItem(
            title: title.trimmingCharacters(in: .whitespacesAndNewlines),
            personName: personName.trimmingCharacters(in: .whitespacesAndNewlines),
            type: type,
            category: category,
            startDate: Int64(Date().timeIntervalSince1970 * 1000),
            dueDate: dueDate,
            notes: notes.trimmingCharacters(in: .whitespacesAndNewlines),
            status: .active,
            latitude: latitude,
            longitude: longitude,
            address: address,
            audioPath: audioPath,
            mediaAttachments: attachments
        )
        repository.addLoan(newLoan)
    }
    
    public func refresh() {
        Task {
            await repository.syncWithFirebase()
        }
    }
}
