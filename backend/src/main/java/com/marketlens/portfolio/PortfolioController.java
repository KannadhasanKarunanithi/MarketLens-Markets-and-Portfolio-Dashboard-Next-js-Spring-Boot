package com.marketlens.portfolio;

import com.marketlens.common.security.CurrentUser;
import com.marketlens.portfolio.dto.PortfolioDtos.AllocationResponse;
import com.marketlens.portfolio.dto.PortfolioDtos.PerformanceResponse;
import com.marketlens.portfolio.dto.PortfolioDtos.PortfolioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PortfolioAnalyticsService analyticsService;

    public PortfolioController(PortfolioService portfolioService,
            PortfolioAnalyticsService analyticsService) {
        this.portfolioService = portfolioService;
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public PortfolioResponse portfolio() {
        return portfolioService.portfolio(CurrentUser.require().id());
    }

    @GetMapping("/allocation")
    public AllocationResponse allocation() {
        return analyticsService.allocation(CurrentUser.require().id());
    }

    @GetMapping("/performance")
    public PerformanceResponse performance(
            @RequestParam(name = "range", required = false) String range) {
        return analyticsService.performance(CurrentUser.require().id(), range);
    }
}
