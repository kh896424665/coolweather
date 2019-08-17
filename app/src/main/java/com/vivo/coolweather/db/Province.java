package com.vivo.coolweather.db;

import org.litepal.crud.LitePalSupport;

public class Province extends LitePalSupport {      //书上是DataSupport，官方文档是LitepalSupport
    private int id;
    private String provinceName;
    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int privinceCode) {
        this.provinceCode = privinceCode;
    }
}
