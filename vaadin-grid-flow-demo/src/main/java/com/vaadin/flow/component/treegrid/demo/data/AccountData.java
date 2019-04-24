package com.vaadin.flow.component.treegrid.demo.data;

import com.vaadin.flow.component.treegrid.demo.entity.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountData {
    private final List<Account> ACCOUNT_LIST = new ArrayList<>();

    {
        ACCOUNT_LIST.add(new Account("1000A", "Assets", null));
        ACCOUNT_LIST.add(new Account("1000B", "Assets", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("1000C", "Assets", ACCOUNT_LIST.get(1)));
        ACCOUNT_LIST.add(new Account("1000D", "Assets", ACCOUNT_LIST.get(2)));
        ACCOUNT_LIST.add(new Account("1000E", "Assets", ACCOUNT_LIST.get(3)));
        ACCOUNT_LIST.add(new Account("10000", "Interunit receivable/payable", ACCOUNT_LIST.get(4)));
        ACCOUNT_LIST.add(new Account("10001", "Interunit suspense", ACCOUNT_LIST.get(4)));
        ACCOUNT_LIST.add(new Account("10050", "SFFDN due to/from BVSF Hldgs", ACCOUNT_LIST.get(4)));
        ACCOUNT_LIST.add(new Account("10100", "Cash-BofA internet cred card", ACCOUNT_LIST.get(4)));
        ACCOUNT_LIST.add(new Account("10105", "Cash-Union Bank - lockbox", ACCOUNT_LIST.get(4)));
        ACCOUNT_LIST.add(new Account("10110", "Cash-BofA campus depository", ACCOUNT_LIST.get(4)));
        ACCOUNT_LIST.add(new Account("10115", "Cash-BofA depository - CES CR", ACCOUNT_LIST.get(4)));
        ACCOUNT_LIST.add(new Account("10120", "Cash-BofA stud loan fed funds", ACCOUNT_LIST.get(4)));
        ACCOUNT_LIST.add(new Account("2000B", "Liabilities", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("2000C", "Liabilities", ACCOUNT_LIST.get(13)));
        ACCOUNT_LIST.add(new Account("2000D", "Liabilities", ACCOUNT_LIST.get(14)));
        ACCOUNT_LIST.add(new Account("2000E", "Liabilities", ACCOUNT_LIST.get(15)));
        ACCOUNT_LIST.add(new Account("20000", "Interunit payable", ACCOUNT_LIST.get(16)));
        ACCOUNT_LIST.add(new Account("20100", "A/P - plant funds", ACCOUNT_LIST.get(16)));
        ACCOUNT_LIST.add(new Account("20120", "A/P-Escrow retention", ACCOUNT_LIST.get(16)));
        ACCOUNT_LIST.add(new Account("20130", "A/P-Payment withholding", ACCOUNT_LIST.get(16)));
        ACCOUNT_LIST.add(new Account("20200", "A/P - auxiliary enterprises", ACCOUNT_LIST.get(16)));
        ACCOUNT_LIST.add(new Account("20401", "S/U tax-Salinas-Monterey Cnty", ACCOUNT_LIST.get(16)));
        ACCOUNT_LIST.add(new Account("3000B", "Net position", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("3000C", "Net position", ACCOUNT_LIST.get(23)));
        ACCOUNT_LIST.add(new Account("3000D", "Net position", ACCOUNT_LIST.get(24)));
        ACCOUNT_LIST.add(new Account("9400", "NET-unexpended plant-gifts", ACCOUNT_LIST.get(25)));
        ACCOUNT_LIST.add(new Account("39405", "3NET-unexp plant-fed grants", ACCOUNT_LIST.get(25)));
        ACCOUNT_LIST.add(new Account("39410", "NET-unexp plant-state appropr", ACCOUNT_LIST.get(25)));
        ACCOUNT_LIST.add(new Account("39415", "NET-unexp plant-university fds", ACCOUNT_LIST.get(25)));
        ACCOUNT_LIST.add(new Account("4000B", "General state appropriations", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("4000C", "General state appropriations", ACCOUNT_LIST.get(30)));
        ACCOUNT_LIST.add(new Account("4000D", "General state appropriations", ACCOUNT_LIST.get(31)));
        ACCOUNT_LIST.add(new Account("4000E", "General state appropriations", ACCOUNT_LIST.get(32)));
        ACCOUNT_LIST.add(new Account("43100", "General state appropriations", ACCOUNT_LIST.get(33)));
        ACCOUNT_LIST.add(new Account("43150", "General state support-research", ACCOUNT_LIST.get(33)));
        ACCOUNT_LIST.add(new Account("74500", "Tfr - state appropriation", ACCOUNT_LIST.get(33)));
        ACCOUNT_LIST.add(new Account("4150B", "Clinical & affiliation revenue", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("4150C", "Clinical funds flow revenue", ACCOUNT_LIST.get(37)));
        ACCOUNT_LIST.add(new Account("4150D", "Clinical funds flow revenue", ACCOUNT_LIST.get(38)));
        ACCOUNT_LIST.add(new Account("4150E", "Clinical funds flow revenue", ACCOUNT_LIST.get(39)));
        ACCOUNT_LIST.add(new Account("74110", "MC-PSA  revenue", ACCOUNT_LIST.get(40)));
        ACCOUNT_LIST.add(new Account("74111", "Tier 1 RVU payment", ACCOUNT_LIST.get(40)));
        ACCOUNT_LIST.add(new Account("74112", "Tier 1 benefit expense", ACCOUNT_LIST.get(40)));
        ACCOUNT_LIST.add(new Account("74113", "Tier 1 pct of net collections", ACCOUNT_LIST.get(40)));
        ACCOUNT_LIST.add(new Account("5080B", "Staff salaries and benefits", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("5050C", "Staff salaries and overtime", ACCOUNT_LIST.get(45)));
        ACCOUNT_LIST.add(new Account("5050D", "Staff salaries and overtime", ACCOUNT_LIST.get(46)));
        ACCOUNT_LIST.add(new Account("5050E", "Staff salaries", ACCOUNT_LIST.get(47)));
        ACCOUNT_LIST.add(new Account("50403", "Regular staff salaries", ACCOUNT_LIST.get(48)));
        ACCOUNT_LIST.add(new Account("50404", "Non-regular staff salaries", ACCOUNT_LIST.get(48)));
        ACCOUNT_LIST.add(new Account("50405", "Term bene s&w invol staff", ACCOUNT_LIST.get(48)));
        ACCOUNT_LIST.add(new Account("50406", "Term bene s&w vol staff", ACCOUNT_LIST.get(48)));
        ACCOUNT_LIST.add(new Account("5060C", "Staff benefits", ACCOUNT_LIST.get(1)));
        ACCOUNT_LIST.add(new Account("5051D", "Staff benefits", ACCOUNT_LIST.get(53)));
        ACCOUNT_LIST.add(new Account("5051E", "Staff vacation leave assess", ACCOUNT_LIST.get(54)));
        ACCOUNT_LIST.add(new Account("50410", "Vacation accrual", ACCOUNT_LIST.get(55)));
        ACCOUNT_LIST.add(new Account("50550", "Vac leave assess-staff", ACCOUNT_LIST.get(55)));
        ACCOUNT_LIST.add(new Account("50551", "VLA salary relief-staff", ACCOUNT_LIST.get(55)));
        ACCOUNT_LIST.add(new Account("50552", "VLA benefits relief-staff", ACCOUNT_LIST.get(55)));
        ACCOUNT_LIST.add(new Account("5060E", "Staff - UCRP benefits", ACCOUNT_LIST.get(3)));
        ACCOUNT_LIST.add(new Account("50532", "UCRS regent contr-staff", ACCOUNT_LIST.get(60)));
        ACCOUNT_LIST.add(new Account("50538", "UCRP supp assess interes-staff", ACCOUNT_LIST.get(60)));
        ACCOUNT_LIST.add(new Account("50553", "UCRP supp assess prin-staff", ACCOUNT_LIST.get(60)));
        ACCOUNT_LIST.add(new Account("50572", "UCRP 2016 DB Supp-staff", ACCOUNT_LIST.get(60)));
        ACCOUNT_LIST.add(new Account("50573", "UCRP 2016 DC Choice-Staff", ACCOUNT_LIST.get(60)));
        ACCOUNT_LIST.add(new Account("50800", "UCRP ARC adjust - UC", ACCOUNT_LIST.get(60)));
        ACCOUNT_LIST.add(new Account("50850", "Pension exp inc after UCRP pay", ACCOUNT_LIST.get(60)));
        ACCOUNT_LIST.add(new Account("50855", "Other pension costs", ACCOUNT_LIST.get(60)));

    }


    public List<Account> getAccounts() {

        return ACCOUNT_LIST;
    }
}
