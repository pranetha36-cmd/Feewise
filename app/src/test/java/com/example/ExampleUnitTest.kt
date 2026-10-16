package com.example

import com.example.data.model.HouseholdBudget
import com.example.data.model.SchoolFeePlan
import com.example.data.model.TermFee
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAnnualFeeCalculation() {
        val terms = listOf(
            TermFee(1, "Term 1", "Monsoon", 12000.0, "2026-11-15"),
            TermFee(2, "Term 2", "Winter", 12000.0, "2027-02-15"),
            TermFee(3, "Term 3", "Spring", 12000.0, "2027-05-15")
        )
        val plan = SchoolFeePlan("Priya Sharma", "Aarav", "DPS", "2026-2027", terms)

        assertEquals(36000.0, plan.totalAnnualFee, 0.01)
    }

    @Test
    fun testSavingsCalculatorFormula() {
        val totalFee = 12000.0
        val saved = 3000.0
        val monthsRemaining = 4

        val remainingFee = (totalFee - saved).coerceAtLeast(0.0)
        assertEquals(9000.0, remainingFee, 0.01)

        val monthlyTarget = remainingFee / monthsRemaining
        assertEquals(2250.0, monthlyTarget, 0.01)

        val weeksRemaining = 17
        val weeklyTarget = remainingFee / weeksRemaining
        assertEquals(529.41, weeklyTarget, 0.01)
    }

    @Test
    fun testHouseholdBudgetSurplus() {
        val budget = HouseholdBudget(
            monthlyIncome = 75000.0,
            rentAndBills = 22000.0,
            groceries = 14000.0,
            transport = 6000.0,
            otherEssentials = 8000.0
        )

        assertEquals(50000.0, budget.totalExpenses, 0.01)
        assertEquals(25000.0, budget.monthlySurplus, 0.01)

        val targetSavings = 3000.0
        assertTrue("Planned school fee savings must be within monthly surplus", targetSavings <= budget.monthlySurplus)
    }
}
