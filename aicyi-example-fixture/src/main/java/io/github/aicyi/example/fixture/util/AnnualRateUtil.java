package io.github.aicyi.example.fixture.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 投资年化收益率计算工具
 */
public class AnnualRateUtil {

    /**
     * 简单年化收益率（基金常用算术年化，和上面例子逻辑一致）
     *
     * @param investStartDate 投资买入日期
     * @param principal       本金
     * @param totalProfit     累计收益
     * @return 年化收益率(小数 ， 如0.0167代表1.67 %)
     */
    public static BigDecimal calcSimpleAnnualRate(LocalDate investStartDate, BigDecimal principal, BigDecimal totalProfit) {
        LocalDate today = LocalDate.now();
        // 持有天数
        long holdDays = ChronoUnit.DAYS.between(investStartDate, today);
        if (holdDays <= 0) {
            return BigDecimal.ZERO;
        }
        // 总收益率 = 累计收益 / 本金
        BigDecimal totalRate = totalProfit.divide(principal, 10, RoundingMode.HALF_UP);
        // 简单年化 = 总收益率 * 365 / holdDays
        BigDecimal annualRate = totalRate.multiply(BigDecimal.valueOf(365))
                .divide(BigDecimal.valueOf(holdDays), 6, RoundingMode.HALF_UP);
        return annualRate;
    }

    /**
     * IRR复利年化收益率（内部收益率，更严谨）
     *
     * @param investStartDate 买入日期
     * @param principal       本金
     * @param totalProfit     累计收益
     * @return IRR年化(小数)
     */
    public static BigDecimal calcIrrAnnualRate(LocalDate investStartDate, BigDecimal principal, BigDecimal totalProfit) {
        LocalDate today = LocalDate.now();
        long holdDays = ChronoUnit.DAYS.between(investStartDate, today);
        if (holdDays <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal finalAsset = principal.add(totalProfit);
        double days = holdDays;
        double yearRatio = days / 365.0;
        // 公式：本金*(1+r)^yearRatio = 期末资产  => r = (期末/本金)^(1/yearRatio) -1
        double p = principal.doubleValue();
        double f = finalAsset.doubleValue();
        double r = Math.pow(f / p, 1.0 / yearRatio) - 1;
        return BigDecimal.valueOf(r).setScale(6, RoundingMode.HALF_UP);
    }


    public static void main(String[] args) {
        // 你的案例：2025‑05‑28买入，本金199000，累计收益3966.91
        LocalDate buyDate = LocalDate.of(2026, 01, 01);
        BigDecimal principal = new BigDecimal("25596");
        BigDecimal profit = new BigDecimal("6105.25");

        BigDecimal simpleRate = calcSimpleAnnualRate(buyDate, principal, profit);
        BigDecimal irrRate = calcIrrAnnualRate(buyDate, principal, profit);

        System.out.println("简单年化收益率:" + simpleRate.multiply(new BigDecimal("100")) + "%");
        System.out.println("IRR复利年化收益率:" + irrRate.multiply(new BigDecimal("100")) + "%");
    }
}