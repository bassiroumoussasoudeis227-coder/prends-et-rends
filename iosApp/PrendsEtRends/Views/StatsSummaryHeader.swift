import SwiftUI

public struct StatsSummaryHeader: View {
    let totalLent: Int
    let totalBorrowed: Int
    let totalOverdue: Int
    let totalReturned: Int
    let onSelectTab: (FilterTab) -> Void
    
    public init(
        totalLent: Int,
        totalBorrowed: Int,
        totalOverdue: Int,
        totalReturned: Int,
        onSelectTab: @escaping (FilterTab) -> Void
    ) {
        self.totalLent = totalLent
        self.totalBorrowed = totalBorrowed
        self.totalOverdue = totalOverdue
        self.totalReturned = totalReturned
        self.onSelectTab = onSelectTab
    }
    
    public var body: some View {
        HStack(spacing: 8) {
            StatCard(
                title: "Prêtés",
                count: totalLent,
                containerColor: Color(red: 0.91, green: 0.96, blue: 0.91),
                contentColor: Color(red: 0.18, green: 0.49, blue: 0.20),
                action: { onSelectTab(.lent) }
            )
            
            StatCard(
                title: "Empruntés",
                count: totalBorrowed,
                containerColor: Color(red: 0.89, green: 0.95, blue: 0.99),
                contentColor: Color(red: 0.08, green: 0.40, blue: 0.75),
                action: { onSelectTab(.borrowed) }
            )
            
            StatCard(
                title: "En retard",
                count: totalOverdue,
                containerColor: totalOverdue > 0 ? Color(red: 1.0, green: 0.92, blue: 0.93) : Color(red: 0.96, green: 0.96, blue: 0.96),
                contentColor: totalOverdue > 0 ? Color(red: 0.78, green: 0.16, blue: 0.16) : Color(red: 0.46, green: 0.46, blue: 0.46),
                action: { onSelectTab(.overdue) }
            )
            
            StatCard(
                title: "Rendus",
                count: totalReturned,
                containerColor: Color(red: 0.95, green: 0.90, blue: 0.96),
                contentColor: Color(red: 0.42, green: 0.11, blue: 0.60),
                action: { onSelectTab(.returned) }
            )
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
}

private struct StatCard: View {
    let title: String
    let count: Int
    let containerColor: Color
    let contentColor: Color
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: 2) {
                Text("\(count)")
                    .font(.system(size: 17, weight: .bold, design: .rounded))
                    .foregroundColor(contentColor)
                Text(title)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(contentColor)
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 10)
            .background(containerColor)
            .cornerRadius(12)
        }
        .buttonStyle(.plain)
    }
}
