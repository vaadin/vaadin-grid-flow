package com.vaadin.flow.component.treegrid.demo.data;

import com.vaadin.flow.component.treegrid.demo.entity.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountData {
    private final List<Account> ACCOUNT_LIST = new ArrayList<>();

    {
        ACCOUNT_LIST.add(new Account("100", "Asset", null));
        ACCOUNT_LIST.add(new Account("101", "Bank/Cash at Bank", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("102", "Cash", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("108", "Deferred Expense", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("110", "Other A52312", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("112", "Accounts Receivable", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("116", "Supplies", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("130", "Prepaid Insurance", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("157", "Equipment", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("158", "Accumulated Depreciation Equipment", ACCOUNT_LIST.get(0)));
        ACCOUNT_LIST.add(new Account("200", "Liability", null));
        ACCOUNT_LIST.add(new Account("201", "Notes Payable", ACCOUNT_LIST.get(10)));
        ACCOUNT_LIST.add(new Account("202", "Accounts Payable", ACCOUNT_LIST.get(10)));
        ACCOUNT_LIST.add(new Account("209", "Unearned Service Revenue", ACCOUNT_LIST.get(10)));
        ACCOUNT_LIST.add(new Account("230", "Interest Payable", ACCOUNT_LIST.get(10)));
        ACCOUNT_LIST.add(new Account("231", "Deferred Gross profit", ACCOUNT_LIST.get(10)));
        ACCOUNT_LIST.add(new Account("300", "Equity", null));
        ACCOUNT_LIST.add(new Account("301", "Equity (for sole proprietorship and partnerships)", ACCOUNT_LIST.get(16)));
        ACCOUNT_LIST.add(new Account("3001", "Owner's capital", ACCOUNT_LIST.get(17)));
        ACCOUNT_LIST.add(new Account("3011", "Share Capital-Ordinary", ACCOUNT_LIST.get(17)));
        ACCOUNT_LIST.add(new Account("3020", "Retained Earnings", ACCOUNT_LIST.get(17)));
        ACCOUNT_LIST.add(new Account("3030", "Capital contributions", ACCOUNT_LIST.get(17)));
        ACCOUNT_LIST.add(new Account("3032", "Dividends", ACCOUNT_LIST.get(17)));
        ACCOUNT_LIST.add(new Account("3050", "Income Summary", ACCOUNT_LIST.get(17)));
        ACCOUNT_LIST.add(new Account("3060", "Drawings (Distributions)", ACCOUNT_LIST.get(17)));
        ACCOUNT_LIST.add(new Account("302", "Equity Accounts (for corporations)", ACCOUNT_LIST.get(16)));
        ACCOUNT_LIST.add(new Account("3001", "Dividend", ACCOUNT_LIST.get(25)));
        ACCOUNT_LIST.add(new Account("3010", "Capital in excess of par", ACCOUNT_LIST.get(25)));
        ACCOUNT_LIST.add(new Account("3030", "Retained earnings", ACCOUNT_LIST.get(25)));
        ACCOUNT_LIST.add(new Account("400", "Revenue", null));
        ACCOUNT_LIST.add(new Account("401", "Rental Income", ACCOUNT_LIST.get(29)));
        ACCOUNT_LIST.add(new Account("410", "Sales Income", ACCOUNT_LIST.get(29)));
        ACCOUNT_LIST.add(new Account("420", "Interest Income", ACCOUNT_LIST.get(29)));
        ACCOUNT_LIST.add(new Account("430", "Other Income", ACCOUNT_LIST.get(29)));
        ACCOUNT_LIST.add(new Account("500", "Expense", null));
        ACCOUNT_LIST.add(new Account("570", "Office Expense", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("585", "Computer Expenses", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("595", "Communication Expense", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("597", "Labour & Welfare Expenses", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("598", "Advertising Expenses", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("599", "Printing & Stationery Expenses", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("507", "Supplies Expense", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("508", "Depreciation Expense", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("509", "Insurance Expense", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("510", "Salaries and Wages Expense", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("511", "Rent Expense", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("512", "Utilities Expense", ACCOUNT_LIST.get(34)));
        ACCOUNT_LIST.add(new Account("513", "Interest Expense", ACCOUNT_LIST.get(34)));
    }


    public List<Account> getAccounts() {

        return ACCOUNT_LIST;
    }

}
