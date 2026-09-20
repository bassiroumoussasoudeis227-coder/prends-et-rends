import SwiftUI

public struct LoansDashboardView: View {
    @StateObject private var viewModel = LoansViewModel()
    @ObservedObject private var i18n = LocalizationManager.shared
    @State private var showingAddSheet = false
    @State private var reminderToShare: String? = nil
    @State private var showingShareSheet = false
    @State private var showingLanguageMenu = false
    
    public init() {}
    
    public var body: some View {
        NavigationView {
            ZStack(alignment: .bottomTrailing) {
                ScrollView {
                    VStack(spacing: 8) {
                        // Header with Cloud status & Language switcher
                        HStack(alignment: .center) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(i18n.string("app_title"))
                                    .font(.system(size: 26, weight: .bold, design: .rounded))
                                Text(i18n.string("app_subtitle"))
                                    .font(.system(size: 13))
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                            
                            // Language Switcher Menu
                            Menu {
                                ForEach(AppLanguage.allCases) { lang in
                                    Button(action: { i18n.currentLanguage = lang }) {
                                        Text("\(lang.flagEmoji) \(lang.displayName)")
                                    }
                                }
                            } label: {
                                Text(i18n.currentLanguage.flagEmoji)
                                    .font(.system(size: 18))
                                    .padding(6)
                                    .background(Color(.secondarySystemBackground))
                                    .clipShape(Circle())
                            }
                            
                            // Cloud Badge
                            HStack(spacing: 4) {
                                Image(systemName: viewModel.isSyncing ? "arrow.triangle.2.circlepath" : "cloud.fill")
                                    .font(.system(size: 11))
                                    .foregroundColor(viewModel.isSyncing ? .accentColor : Color(red: 0.18, green: 0.49, blue: 0.20))
                                Text(viewModel.isSyncing ? i18n.string("cloud_syncing") : i18n.string("cloud_synced"))
                                    .font(.system(size: 11, weight: .medium))
                                    .foregroundColor(.secondary)
                            }
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(12)
                            
                            Button(action: { viewModel.refresh() }) {
                                Image(systemName: "arrow.clockwise")
                                    .font(.system(size: 14, weight: .semibold))
                                    .padding(8)
                                    .background(Color(.secondarySystemBackground))
                                    .clipShape(Circle())
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.top, 4)
                        
                        // 4 KPI Summary Cards
                        StatsSummaryHeader(
                            totalLent: viewModel.totalLent,
                            totalBorrowed: viewModel.totalBorrowed,
                            totalOverdue: viewModel.totalOverdue,
                            totalReturned: viewModel.totalReturned,
                            onSelectTab: { tab in viewModel.selectedFilter = tab }
                        )
                        
                        // Search bar
                        HStack {
                            Image(systemName: "magnifyingglass")
                                .foregroundColor(.secondary)
                            TextField(i18n.string("search_placeholder"), text: $viewModel.searchQuery)
                            if !viewModel.searchQuery.isEmpty {
                                Button(action: { viewModel.searchQuery = "" }) {
                                    Image(systemName: "xmark.circle.fill")
                                        .foregroundColor(.secondary)
                                }
                            }
                        }
                        .padding(10)
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(12)
                        .padding(.horizontal, 16)
                        
                        // Filter Tabs
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(FilterTab.allCases) { tab in
                                    let isSelected = viewModel.selectedFilter == tab
                                    let badgeCount: String = {
                                        switch tab {
                                        case .all: return ""
                                        case .lent: return " (\(viewModel.totalLent))"
                                        case .borrowed: return " (\(viewModel.totalBorrowed))"
                                        case .overdue: return viewModel.totalOverdue > 0 ? " (\(viewModel.totalOverdue))" : ""
                                        case .returned: return " (\(viewModel.totalReturned))"
                                        }
                                    }()
                                    
                                    Button(action: { viewModel.selectedFilter = tab }) {
                                        Text("\(tab.rawValue)\(badgeCount)")
                                            .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                                            .padding(.horizontal, 12)
                                            .padding(.vertical, 6)
                                            .background(isSelected ? Color.accentColor : Color(.secondarySystemBackground))
                                            .foregroundColor(isSelected ? .white : .primary)
                                            .cornerRadius(16)
                                    }
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                        .padding(.vertical, 2)
                        
                        // Categories horizontal filter chips
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 6) {
                                ForEach(LoanCategory.allCases, id: \.self) { cat in
                                    let isSelected = viewModel.selectedCategory == cat
                                    Button(action: { viewModel.toggleCategory(cat) }) {
                                        HStack(spacing: 4) {
                                            Image(systemName: cat.systemIcon)
                                                .font(.system(size: 11))
                                            Text(cat.label)
                                                .font(.system(size: 11, weight: isSelected ? .semibold : .regular))
                                        }
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 5)
                                        .background(isSelected ? Color.accentColor.opacity(0.18) : Color(.tertiarySystemBackground))
                                        .foregroundColor(isSelected ? Color.accentColor : .secondary)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke(isSelected ? Color.accentColor : Color.clear, lineWidth: 1)
                                        )
                                        .cornerRadius(12)
                                    }
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                        
                        // Loans Items List or Empty State
                        if viewModel.filteredItems.isEmpty {
                            VStack(spacing: 12) {
                                Image(systemName: "tray")
                                    .font(.system(size: 44))
                                    .foregroundColor(.secondary)
                                Text("Aucun élément")
                                    .font(.system(size: 16, weight: .bold))
                                Text(emptyMessage(for: viewModel.selectedFilter))
                                    .font(.system(size: 13))
                                    .foregroundColor(.secondary)
                                    .multilineTextAlignment(.center)
                                    .padding(.horizontal, 32)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.top, 48)
                        } else {
                            LazyVStack(spacing: 10) {
                                ForEach(viewModel.filteredItems) { item in
                                    LoanCardView(
                                        item: item,
                                        onToggleStatus: { viewModel.toggleStatus(for: item.id) },
                                        onDelete: { viewModel.deleteLoan(id: item.id) },
                                        onShareReminder: {
                                            self.reminderToShare = createReminderMessage(for: item)
                                            self.showingShareSheet = true
                                        }
                                    )
                                }
                            }
                            .padding(.horizontal, 16)
                            .padding(.top, 6)
                            .padding(.bottom, 80)
                        }
                    }
                }
                
                // Floating Action Button
                Button(action: { showingAddSheet = true }) {
                    HStack(spacing: 6) {
                        Image(systemName: "plus")
                            .font(.system(size: 16, weight: .bold))
                        Text(i18n.string("new_loan"))
                            .font(.system(size: 15, weight: .bold))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 12)
                    .background(Color.accentColor)
                    .clipShape(Capsule())
                    .shadow(color: Color.accentColor.opacity(0.35), radius: 8, x: 0, y: 4)
                }
                .padding(.trailing, 20)
                .padding(.bottom, 20)
            }
            .navigationBarHidden(true)
            .sheet(isPresented: $showingAddSheet) {
                AddLoanSheet { title, person, type, category, dueDate, notes, lat, lon, addr, audio, media in
                    viewModel.saveLoan(
                        title: title,
                        personName: person,
                        type: type,
                        category: category,
                        dueDate: dueDate,
                        notes: notes,
                        latitude: lat,
                        longitude: lon,
                        address: addr,
                        audioPath: audio,
                        attachments: media
                    )
                }
            }
            .sheet(isPresented: $showingShareSheet) {
                if let text = reminderToShare {
                    ShareActivityView(activityItems: [text])
                }
            }
        }
    }
    
    private func emptyMessage(for filter: FilterTab) -> String {
        switch filter {
        case .all: return "Vous n'avez aucun prêt ou emprunt enregistré."
        case .lent: return "Vous n'avez prêté aucun objet actuellement."
        case .borrowed: return "Vous n'avez aucun emprunt en cours."
        case .overdue: return "Super ! Aucun prêt ou emprunt en retard."
        case .returned: return "Aucun objet n'a encore été marqué comme rendu."
        }
    }
    
    private func createReminderMessage(for item: LoanItem) -> String {
        let df = DateFormatter()
        df.locale = Locale(identifier: "fr_FR")
        df.dateFormat = "dd/MM/yyyy"
        let dueStr = item.dueDate != nil ? "pour le \(df.string(from: Date(timeIntervalSince1970: TimeInterval(item.dueDate! / 1000))))" : "prochainement"
        return "Hello \(item.personName) ! Petit rappel amical concernant '\(item.title)' que je t'ai prêté, à me rendre \(dueStr). Merci beaucoup !"
    }
}

// Share sheet wrapper
struct ShareActivityView: UIViewControllerRepresentable {
    let activityItems: [Any]
    
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }
    
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
