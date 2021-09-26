package com.chuxinbuer.deyiguanjia.utils;

public interface MainContract {

    interface IView {
        void setOpen(boolean isOpen);

        void addData(String result);

        void showPermissionDialog();

        String getEditText();
    }

    interface IPresenter {

    }

}