package com.example.data

import com.example.model.LoanCategory
import com.example.model.LoanItem
import com.example.model.LoanStatus
import com.example.model.LoanType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoansRepository(
    private val firebaseService: FirebaseLoansService = FirebaseLoansService(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _loans = MutableStateFlow<List<LoanItem>>(getInitialSeedData())
    val loans: StateFlow<List<LoanItem>> = _loans.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Start listening to Firebase real-time updates if available
        firebaseService.observeLoans { remoteLoans ->
            if (remoteLoans.isNotEmpty()) {
                _loans.value = remoteLoans
            }
        }
        // Initial fetch
        syncWithFirebase()
    }

    fun syncWithFirebase() {
        if (!firebaseService.isFirebaseAvailable) return
        scope.launch {
            _isSyncing.value = true
            try {
                val remoteLoans = firebaseService.fetchAllLoans()
                if (remoteLoans.isNotEmpty()) {
                    _loans.value = remoteLoans
                } else if (_loans.value.isNotEmpty()) {
                    // Seed remote with initial data
                    _loans.value.forEach { firebaseService.saveLoan(it) }
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    suspend fun addLoan(loan: LoanItem) {
        val updated = listOf(loan) + _loans.value
        _loans.value = updated
        firebaseService.saveLoan(loan)
    }

    suspend fun updateLoan(loan: LoanItem) {
        val updated = _loans.value.map { if (it.id == loan.id) loan else it }
        _loans.value = updated
        firebaseService.saveLoan(loan)
    }

    suspend fun toggleStatus(loanId: String) {
        val loan = _loans.value.find { it.id == loanId } ?: return
        val newStatus = if (loan.status == LoanStatus.ACTIVE) LoanStatus.RETURNED else LoanStatus.ACTIVE
        val returnedDate = if (newStatus == LoanStatus.RETURNED) System.currentTimeMillis() else null
        val updatedLoan = loan.copy(status = newStatus, returnedDate = returnedDate)
        updateLoan(updatedLoan)
    }

    suspend fun deleteLoan(loanId: String) {
        _loans.value = _loans.value.filter { it.id != loanId }
        firebaseService.deleteLoan(loanId)
    }

    private fun getInitialSeedData(): List<LoanItem> {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        return listOf(
            LoanItem(
                id = "loan-1",
                title = "Perceuse à percussion Bosch",
                personName = "Thomas Dubois",
                type = LoanType.LENT,
                category = LoanCategory.TOOLS,
                startDate = now - 5 * oneDay,
                dueDate = now + 4 * oneDay,
                notes = "Avec le jeu de mèches à béton",
                status = LoanStatus.ACTIVE,
                latitude = 48.8566,
                longitude = 2.3522,
                address = "12 Rue de Rivoli, Paris, France"
            ),
            LoanItem(
                id = "loan-2",
                title = "Livre Clean Architecture",
                personName = "Sophie Martin",
                type = LoanType.BORROWED,
                category = LoanCategory.BOOKS,
                startDate = now - 10 * oneDay,
                dueDate = now + 2 * oneDay,
                notes = "À rendre après le chapitre 7",
                status = LoanStatus.ACTIVE,
                latitude = 45.7640,
                longitude = 4.8357,
                address = "Place Bellecour, Lyon, France"
            ),
            LoanItem(
                id = "loan-3",
                title = "Appareil à raclette 8 pers.",
                personName = "Camille Laurent",
                type = LoanType.BORROWED,
                category = LoanCategory.OTHER,
                startDate = now - 12 * oneDay,
                dueDate = now - 2 * oneDay, // en retard!
                notes = "Soirée chez nous le week-end dernier",
                status = LoanStatus.ACTIVE,
                latitude = 43.6047,
                longitude = 1.4442,
                address = "Place du Capitole, Toulouse, France"
            ),
            LoanItem(
                id = "loan-4",
                title = "Manette Switch Pro",
                personName = "Marc Lefevre",
                type = LoanType.LENT,
                category = LoanCategory.GAMES,
                startDate = now - 20 * oneDay,
                dueDate = now - 5 * oneDay,
                notes = "Pour tester Mario Wonder",
                status = LoanStatus.RETURNED,
                returnedDate = now - 4 * oneDay,
                latitude = 47.2184,
                longitude = -1.5536,
                address = "Nantes Centre, France"
            ),
            LoanItem(
                id = "loan-5",
                title = "Enceinte Bluetooth JBL",
                personName = "Alexandre Petit",
                type = LoanType.LENT,
                category = LoanCategory.HIGH_TECH,
                startDate = now - 2 * oneDay,
                dueDate = now + 10 * oneDay,
                notes = "Pour son barbecue",
                status = LoanStatus.ACTIVE,
                latitude = 50.6292,
                longitude = 3.0573,
                address = "Grand Place, Lille, France"
            )
        )
    }
}
