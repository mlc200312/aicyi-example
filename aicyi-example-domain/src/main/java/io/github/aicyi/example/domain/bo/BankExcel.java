package io.github.aicyi.example.domain.bo;

import io.github.aicyi.commons.lang.BoBean;
import io.github.aicyi.commons.lang.model.BaseBean;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankExcel extends BaseBean implements BoBean {
    //id	bankEnAbbr	bankEnName	countryCode	serviceCountryCode	bankType	bankCode
    private String id;
    private String bankEnAbbr;
    private String bankEnName;
    private String countryCode;
    private String serviceCountryCode;
    private String bankType;
    private String bankCode;
}