package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstallCohortTest {
    @Test
    public void metaMarketingLink_isMarketing() {
        assertTrue(InstallCohort.isPaidReferrer(
                "utm_source=facebook&utm_medium=cpc&utm_campaign=meta"));
        assertTrue(InstallCohort.isPaidReferrer(
                "utm_source=apps.facebook.com&utm_campaign=Facebook_Android"));
        assertTrue(InstallCohort.isPaidReferrer("fbclid=abc"));
        assertTrue(InstallCohort.isPaidReferrer("gclid=xyz"));
    }

    @Test
    public void organicPlay_isNotMarketing() {
        assertFalse(InstallCohort.isPaidReferrer(""));
        assertFalse(InstallCohort.isPaidReferrer(
                "utm_source=google-play&utm_medium=organic"));
    }
}
