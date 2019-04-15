package com.vaadin.flow.component.treegrid.demo.data;

import com.vaadin.flow.component.treegrid.demo.entity.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountData {
    private final List<Account> Account_LIST = new ArrayList<>();

    {
        Account_LIST.add(new Account("1000A", "Assets", null));
        Account_LIST.add(new Account("1000B", "Assets", Account_LIST.get(0)));
        Account_LIST.add(new Account("1000C", "Assets", Account_LIST.get(1)));
        Account_LIST.add(new Account("1000D", "Assets", Account_LIST.get(2)));
        Account_LIST.add(new Account("1000E", "Assets", Account_LIST.get(3)));
        Account_LIST.add(new Account("10000", "Interunit receivable/payable", Account_LIST.get(4)));
        Account_LIST.add(new Account("10001", "Interunit suspense", Account_LIST.get(4)));
        Account_LIST.add(new Account("10050", "SFFDN due to/from BVSF Hldgs", Account_LIST.get(4)));
        Account_LIST.add(new Account("10100", "Cash-BofA internet cred card", Account_LIST.get(4)));
        Account_LIST.add(new Account("10105", "Cash-Union Bank - lockbox", Account_LIST.get(4)));
        Account_LIST.add(new Account("10110", "Cash-BofA campus depository", Account_LIST.get(4)));
        Account_LIST.add(new Account("10115", "Cash-BofA depository - CES CR", Account_LIST.get(4)));
        Account_LIST.add(new Account("10120", "Cash-BofA stud loan fed funds", Account_LIST.get(4)));
        Account_LIST.add(new Account("2000B", "Liabilities", Account_LIST.get(0)));
        Account_LIST.add(new Account("2000C", "Liabilities", Account_LIST.get(13)));
        Account_LIST.add(new Account("2000D", "Liabilities", Account_LIST.get(14)));
        Account_LIST.add(new Account("2000E", "Liabilities", Account_LIST.get(15)));
        Account_LIST.add(new Account("20000", "Interunit payable", Account_LIST.get(16)));
        Account_LIST.add(new Account("20100", "A/P - plant funds", Account_LIST.get(16)));
        Account_LIST.add(new Account("20120", "A/P-Escrow retention", Account_LIST.get(16)));
        Account_LIST.add(new Account("20130", "A/P-Payment withholding", Account_LIST.get(16)));
        Account_LIST.add(new Account("20200", "A/P - auxiliary enterprises", Account_LIST.get(16)));
        Account_LIST.add(new Account("20401", "S/U tax-Salinas-Monterey Cnty", Account_LIST.get(16)));
        Account_LIST.add(new Account("3000B", "Net position", Account_LIST.get(0)));
        Account_LIST.add(new Account("3000C", "Net position", Account_LIST.get(23)));
        Account_LIST.add(new Account("3000D", "Net position", Account_LIST.get(24)));
        Account_LIST.add(new Account("9400", "NET-unexpended plant-gifts", Account_LIST.get(25)));
        Account_LIST.add(new Account("39405", "3NET-unexp plant-fed grants", Account_LIST.get(25)));
        Account_LIST.add(new Account("39410", "NET-unexp plant-state appropr", Account_LIST.get(25)));
        Account_LIST.add(new Account("39415", "NET-unexp plant-university fds", Account_LIST.get(25)));
        Account_LIST.add(new Account("4000B", "General state appropriations", Account_LIST.get(0)));
        Account_LIST.add(new Account("4000C", "General state appropriations", Account_LIST.get(30)));
        Account_LIST.add(new Account("4000D", "General state appropriations", Account_LIST.get(31)));
        Account_LIST.add(new Account("4000E", "General state appropriations", Account_LIST.get(32)));
        Account_LIST.add(new Account("43100", "General state appropriations", Account_LIST.get(33)));
        Account_LIST.add(new Account("43150", "General state support-research", Account_LIST.get(33)));
        Account_LIST.add(new Account("74500", "Tfr - state appropriation", Account_LIST.get(33)));
        Account_LIST.add(new Account("4150B", "Clinical & affiliation revenue", Account_LIST.get(0)));
        Account_LIST.add(new Account("4150C", "Clinical funds flow revenue", Account_LIST.get(37)));
        Account_LIST.add(new Account("4150D", "Clinical funds flow revenue", Account_LIST.get(38)));
        Account_LIST.add(new Account("4150E", "Clinical funds flow revenue", Account_LIST.get(39)));
        Account_LIST.add(new Account("74110", "MC-PSA  revenue", Account_LIST.get(40)));
        Account_LIST.add(new Account("74111", "Tier 1 RVU payment", Account_LIST.get(40)));
        Account_LIST.add(new Account("74112", "Tier 1 benefit expense", Account_LIST.get(40)));
        Account_LIST.add(new Account("74113", "Tier 1 pct of net collections", Account_LIST.get(40)));
        Account_LIST.add(new Account("5080B", "Staff salaries and benefits", Account_LIST.get(0)));
        Account_LIST.add(new Account("5050C", "Staff salaries and overtime", Account_LIST.get(45)));
        Account_LIST.add(new Account("5050D", "Staff salaries and overtime", Account_LIST.get(46)));
        Account_LIST.add(new Account("5050E", "Staff salaries", Account_LIST.get(47)));
        Account_LIST.add(new Account("50403", "Regular staff salaries", Account_LIST.get(48)));
        Account_LIST.add(new Account("50404", "Non-regular staff salaries", Account_LIST.get(48)));
        Account_LIST.add(new Account("50405", "Term bene s&w invol staff", Account_LIST.get(48)));
        Account_LIST.add(new Account("50406", "Term bene s&w vol staff", Account_LIST.get(48)));
        Account_LIST.add(new Account("5060C", "Staff benefits", Account_LIST.get(1)));
        Account_LIST.add(new Account("5051D", "Staff benefits", Account_LIST.get(53)));
        Account_LIST.add(new Account("5051E", "Staff vacation leave assess", Account_LIST.get(54)));
        Account_LIST.add(new Account("50410", "Vacation accrual", Account_LIST.get(55)));
        Account_LIST.add(new Account("50550", "Vac leave assess-staff", Account_LIST.get(55)));
        Account_LIST.add(new Account("50551", "VLA salary relief-staff", Account_LIST.get(55)));
        Account_LIST.add(new Account("50552", "VLA benefits relief-staff", Account_LIST.get(55)));
        Account_LIST.add(new Account("5060E", "Staff - UCRP benefits", Account_LIST.get(3)));
        Account_LIST.add(new Account("50532", "UCRS regent contr-staff", Account_LIST.get(60)));
        Account_LIST.add(new Account("50538", "UCRP supp assess interes-staff", Account_LIST.get(60)));
        Account_LIST.add(new Account("50553", "UCRP supp assess prin-staff", Account_LIST.get(60)));
        Account_LIST.add(new Account("50572", "UCRP 2016 DB Supp-staff", Account_LIST.get(60)));
        Account_LIST.add(new Account("50573", "UCRP 2016 DC Choice-Staff", Account_LIST.get(60)));
        Account_LIST.add(new Account("50800", "UCRP ARC adjust - UC", Account_LIST.get(60)));
        Account_LIST.add(new Account("50850", "Pension exp inc after UCRP pay", Account_LIST.get(60)));
        Account_LIST.add(new Account("50855", "Other pension costs", Account_LIST.get(60)));

    }


    public List<Account> getAccounts() {

        return Account_LIST;
    }
}
