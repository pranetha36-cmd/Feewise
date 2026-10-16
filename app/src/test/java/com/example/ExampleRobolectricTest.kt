package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.FeeWiseRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FeeWise", appName)
    }

    @Test
    fun `repository sample plan initialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = FeeWiseRepository(context)
        val plan = repo.feePlan.value

        assertEquals(3, plan.terms.size)
        assertEquals(36000.0, plan.totalAnnualFee, 0.01)
        assertNotNull(repo.budget.value)
    }
}
