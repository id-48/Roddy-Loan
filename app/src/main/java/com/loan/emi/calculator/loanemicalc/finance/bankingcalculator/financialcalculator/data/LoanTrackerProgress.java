package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data;

/**
 * Day-based loan tracker timeline from submission:
 * day 0–1 → Step 1 Application Received
 * day 2   → Step 2 Processing
 * day 3–4 → Step 3 Complete
 * day 5+  → Step 4 Loan Rejected (not eligible)
 */
public final class LoanTrackerProgress {
    private static final long DAY_MS = 24L * 60L * 60L * 1000L;
    public static final String REJECT_REASON = "You are not eligible for our criteria";

    public final int step;          // 1..4
    public final int progress;      // 25/50/75/100
    public final String homeSubtitle;
    public final String badge;
    public final boolean rejected;
    public final String notifyTitle;
    public final String notifyBody;

    private LoanTrackerProgress(int step, int progress, String homeSubtitle, String badge,
                                boolean rejected, String notifyTitle, String notifyBody) {
        this.step = step;
        this.progress = progress;
        this.homeSubtitle = homeSubtitle;
        this.badge = badge;
        this.rejected = rejected;
        this.notifyTitle = notifyTitle;
        this.notifyBody = notifyBody;
    }

    public static int daysSinceSubmission(long submissionTimeMs, long nowMs) {
        if (submissionTimeMs <= 0L) {
            return 0;
        }
        long elapsed = Math.max(0L, nowMs - submissionTimeMs);
        return (int) (elapsed / DAY_MS);
    }

    public static LoanTrackerProgress fromSubmission(long submissionTimeMs) {
        return fromSubmission(submissionTimeMs, System.currentTimeMillis());
    }

    public static LoanTrackerProgress fromSubmission(long submissionTimeMs, long nowMs) {
        int days = daysSinceSubmission(submissionTimeMs, nowMs);
        if (days >= 5) {
            return new LoanTrackerProgress(
                    4, 100,
                    "Step 4: Loan Rejected",
                    "REJECTED",
                    true,
                    "Loan Rejected",
                    REJECT_REASON);
        }
        if (days >= 3) {
            return new LoanTrackerProgress(
                    3, 75,
                    "Step 3: Complete",
                    "LIVE",
                    false,
                    "Application Update",
                    "Underwriting is complete. Final decision is pending.");
        }
        if (days >= 2) {
            return new LoanTrackerProgress(
                    2, 50,
                    "Step 2: Processing",
                    "LIVE",
                    false,
                    "Application Update",
                    "Your application is now being processed.");
        }
        return new LoanTrackerProgress(
                1, 25,
                "Step 1: Application Received",
                "LIVE",
                false,
                "Application Received",
                "Your loan application has been received.");
    }

    /** Status label for History timeline row (1..4). */
    public String stepStatus(int stepIndex) {
        if (stepIndex < 1 || stepIndex > 4) {
            return "Pending";
        }
        if (rejected) {
            if (stepIndex < 4) {
                return "Completed";
            }
            return REJECT_REASON;
        }
        if (stepIndex < step) {
            return "Completed";
        }
        if (stepIndex == step) {
            if (step == 3) {
                return "Completed";
            }
            return "In progress";
        }
        if (stepIndex == step + 1) {
            return step >= 3 ? "Pending" : "Queued";
        }
        return "Pending";
    }

    public boolean isStepComplete(int stepIndex) {
        if (rejected) {
            return stepIndex < 4;
        }
        if (step == 3 && stepIndex == 3) {
            return true;
        }
        return stepIndex < step;
    }
}
