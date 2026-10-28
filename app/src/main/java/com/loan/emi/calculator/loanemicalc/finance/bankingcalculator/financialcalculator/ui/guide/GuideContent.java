package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.guide;

import java.util.HashMap;
import java.util.Map;

final class GuideContent {
    private static final Map<String, String> CONTENT = new HashMap<>();

    static {
        put("Personal Loan",
                "A personal loan is an unsecured loan for personal expenses like travel, wedding, medical bills or debt consolidation.\n\n"
                        + "Key points:\n"
                        + "• Usually no collateral required\n"
                        + "• Tenure often 12–60 months\n"
                        + "• Interest depends on credit score and income\n"
                        + "• EMI stays fixed in most floating/fixed retail products\n\n"
                        + "Tip: Keep your EMI under 40–50% of monthly take-home income.");
        put("Home Loan",
                "A home loan helps you buy or construct a house with property as security.\n\n"
                        + "Key points:\n"
                        + "• Longer tenure (up to 20–30 years)\n"
                        + "• Lower rates than personal loans\n"
                        + "• Tax benefits may apply under local laws\n"
                        + "• Processing fee and valuation charges may apply\n\n"
                        + "Tip: Compare floating vs fixed rates and prepayment rules.");
        put("Car Loan",
                "An auto loan finances car purchase with the vehicle as security.\n\n"
                        + "Key points:\n"
                        + "• Down payment usually 10–20%\n"
                        + "• Tenure commonly 1–7 years\n"
                        + "• Insurance is often mandatory\n\n"
                        + "Tip: Negotiate on-road price before finalising loan amount.");
        put("Education Loan",
                "Education loans cover tuition and related study expenses.\n\n"
                        + "Key points:\n"
                        + "• Moratorium during study is common\n"
                        + "• Co-applicant may be required\n"
                        + "• Interest may be lower with collateral\n\n"
                        + "Tip: Track disbursement schedule with your college.");
        put("Business Loan",
                "Business loans support working capital, expansion or equipment.\n\n"
                        + "Key points:\n"
                        + "• Documents include bank statements and GST/ITR where applicable\n"
                        + "• Amount depends on turnover and cash flow\n"
                        + "• Can be secured or unsecured\n\n"
                        + "Tip: Keep business banking history clean for better offers.");
        put("Gold Loan",
                "Gold loans provide quick funds against gold ornaments.\n\n"
                        + "Key points:\n"
                        + "• Fast disbursal\n"
                        + "• LTV usually 60–75% of gold value\n"
                        + "• Short to medium tenure\n\n"
                        + "Tip: Compare purity checks and auction clauses.");
        put("How EMI works",
                "EMI (Equated Monthly Instalment) is a fixed monthly payment covering principal + interest.\n\n"
                        + "Formula idea:\n"
                        + "EMI = P × r × (1+r)^n / ((1+r)^n − 1)\n"
                        + "where P = loan amount, r = monthly rate, n = months.\n\n"
                        + "Early EMIs have higher interest share; later EMIs repay more principal.");
        put("Tips to get loan",
                "Improve approval chances:\n"
                        + "• Maintain CIBIL/credit score above 750\n"
                        + "• Keep stable income proof ready\n"
                        + "• Reduce existing EMI burden\n"
                        + "• Avoid multiple loan enquiries in a short period\n"
                        + "• Choose realistic amount and tenure");
        put("Documents required",
                "Common documents:\n"
                        + "• KYC: Aadhaar/PAN/Passport\n"
                        + "• Income: salary slips / ITR / bank statements\n"
                        + "• Address proof\n"
                        + "• Photograph and application form\n\n"
                        + "Self-employed applicants may need business proofs and GST returns.");
        put("CIBIL score tips",
                "Credit score tips:\n"
                        + "• Pay EMIs and credit cards on time\n"
                        + "• Keep credit utilisation under 30%\n"
                        + "• Don’t close oldest credit accounts hastily\n"
                        + "• Review credit report for errors yearly");
        put("Prepayment benefits",
                "Prepaying reduces interest cost and tenure.\n\n"
                        + "Benefits:\n"
                        + "• Lower total interest\n"
                        + "• Faster debt-free timeline\n"
                        + "• Better cash-flow flexibility\n\n"
                        + "Check if your lender charges prepayment fees.");
        put("What is EPF",
                "EPF (Employees’ Provident Fund) is a retirement savings scheme where employee and employer contribute monthly.\n\n"
                        + "It earns interest and can be withdrawn under specific conditions like retirement, unemployment or housing.");
        put("How to withdraw",
                "EPF withdrawal can be online via the member portal/UMANG with UAN, KYC and bank details linked.\n\n"
                        + "Partial withdrawals are allowed for housing, education, medical needs as per rules.");
        put("EPF balance check",
                "Check EPF balance using UAN portal, UMANG app, or missed-call/SMS services where available.\n\n"
                        + "Ensure your mobile number is linked to UAN for OTP-based access.");
        put("Transfer EPF",
                "When changing jobs, transfer previous EPF to the new account using online transfer claim so service continuity and interest continue.");
        put("EPF interest rate",
                "EPF interest is declared periodically by the authorities and credited to member accounts yearly. Rates can change across financial years.");
        put("Term Insurance",
                "Term insurance provides life cover for a fixed term at relatively low premiums. Ideal for income replacement for dependents.");
        put("Endowment",
                "Endowment plans combine insurance with savings. They pay a lump sum on maturity or death, usually with lower pure cover than term plans.");
        put("ULIP",
                "ULIPs invest part of premium in market-linked funds and provide life cover. Returns vary with markets; charges apply.");
        put("Whole Life",
                "Whole life insurance covers you for lifetime (or up to a high age) and may include savings components depending on the product.");
        put("Health Insurance",
                "Health insurance covers hospitalisation and medical costs. Check waiting periods, room rent limits and network hospitals.");
        put("Motor Insurance",
                "Motor insurance covers third-party liability (mandatory) and optional own-damage for your vehicle.");
        put("Home Insurance",
                "Home insurance protects structure and/or contents against fire, flood, theft and related risks.");
        put("Travel Insurance",
                "Travel insurance covers trip medical emergencies, baggage loss and cancellations while travelling.");
    }

    private GuideContent() {
    }

    private static void put(String key, String value) {
        CONTENT.put(key, value);
    }

    static String forTitle(String title) {
        if (title == null) {
            return "Guide content will appear here.";
        }
        String body = CONTENT.get(title);
        if (body != null) {
            return body;
        }
        return "Learn more about " + title + ".\n\nThis guide covers eligibility basics, documents, costs and practical tips so you can take a confident next step.";
    }

    static String subtitleFor(String title) {
        return "Tap to read full guide";
    }
}
