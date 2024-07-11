package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.response.PackagingCost;
import kz.wonder.wonderuserrepository.dto.response.StartPackageResponse;
import kz.wonder.wonderuserrepository.security.authorizations.base.StoreEmployeeAuthorization;
import kz.wonder.wonderuserrepository.services.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@StoreEmployeeAuthorization
@RequestMapping("/order-packages")
public class PackageController {
    private final PackageService packageService;


    @PostMapping("/{orderCode}/start")
    public ResponseEntity<StartPackageResponse> startPackageOrder(@PathVariable String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        StartPackageResponse startPackageResponse =  packageService.startPackaging(orderCode, keycloakId);

        return ResponseEntity.status(HttpStatus.CREATED).body(startPackageResponse);
    }

    @PostMapping("/{orderCode}/finish")
    public ResponseEntity<Void> finishPackageOrder(@PathVariable String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        packageService.finishPackaging(orderCode, keycloakId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{orderCode}/package-product/start")
    public ResponseEntity<Void> packageProductStart(@PathVariable String orderCode, @RequestParam("product-article") String productArticle) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        packageService.packageProductStart(orderCode, productArticle, keycloakId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{orderCode}/package-product/finish")
    public ResponseEntity<Void> packageProductEnd(@PathVariable String orderCode, @RequestParam("product-article") String productArticle) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        packageService.packageProductFinish(orderCode, productArticle, keycloakId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/calculate")
    public PackagingCost calculatePackagingCost(
            @RequestParam double length,
            @RequestParam double width,
            @RequestParam double height,
            @RequestParam double weight,
            @RequestParam int bubbleWrapTurn,
            @RequestParam int stretchTurn,
            @RequestParam int tapeType,
            @RequestParam int manipulativeSignNeed
    ) {
        length /= 100;
        width /= 100;
        height /= 100;

        // Calculate surface area and perimeter
        double surfaceArea = 2 * (length * width + length * height + width * height);
        double perimeter = 2 * (length + width);

        // Calculate tape length
        double tapeLength = (2 * (length + width) + 2 * (length + height) + 2 * ((width + height) * (1 + (length / (MAX_LENGTH / 100)))));

        // Calculate packaging time
        double kBubbleWrap = 0.1;  // min/m²
        double kTape = 0.02;  // min/m
        double kStretchFilm = 0.05;  // min/m²
        double baseWeight = 1;  // kg

        // Weight factor
        double weightFactor = 1 + (weight / baseWeight);

        // Time calculations
        double tBubbleWrap = kBubbleWrap * surfaceArea * weightFactor;
        double tTape = kTape * 8 * perimeter * weightFactor;
        double tStretchFilm = kStretchFilm * 2 * surfaceArea * weightFactor;

        // Total packaging time
        double tTotal = tBubbleWrap + tTape + tStretchFilm;
        int packagingMinutes = (int) tTotal;
        int packagingSeconds = (int) ((tTotal - packagingMinutes) * 60);

        // Calculate labor cost per package
        double hoursPerMonth = WORK_HOURS_PER_WEEK * WEEKS_PER_MONTH;
        double hourlyWage = MONTHLY_SALARY / hoursPerMonth;
        double costPerPackageLabor = hourlyWage * (tTotal / 60);  // Convert time to hours

        // Calculate tape cost
        double tapeCost;
        if (tapeType == 1) {
            tapeCost = tapeLength * (TAPE_FRAGILE_COST / TAPE_FRAGILE_LENGTH);
        } else {
            tapeCost = tapeLength * (TAPE_COST / TAPE_LENGTH);
        }

        // Calculate label cost
        double labelCost = LABEL_COST / LABELS_PER_ROLL;

        // Calculate bubble wrap cost (example calculation, not including the detailed logic for finding the cheapest combination)
        double bubbleWrapCost = (length * width * bubbleWrapTurn) * (BUBBLE_WRAP_SMALL_COST / BUBBLE_WRAP_SMALL_LENGTH);

        // Calculate stretch film cost
        double stretchFilmCost = (stretchTurn * (length + width + height)) * (STRETCH_FILM_COST / STRETCH_FILM_LENGTH);

        // Calculate manipulative sign cost
        double manipulativeSignCost = (manipulativeSignNeed == 1) ? (MANIPULATIVE_SIGN_COST / MANIPULATIVE_SIGN_COUNT) : 0;

        // Calculate courier package cost
        double courierPackageCost = (length * 100 < COURIER_PACKAGE_SIZE_CM_LENGTH || width * 100 < COURIER_PACKAGE_SIZE_CM_WIDTH) ? (COURIER_PACKAGE_COST / COURIER_PACKAGE_COUNT) : 0;

        // Total cost
        double totalCost = bubbleWrapCost + tapeCost + costPerPackageLabor + courierPackageCost + stretchFilmCost + manipulativeSignCost;

        return new PackagingCost(totalCost, packagingMinutes, packagingSeconds);
    }


//    @GetMapping()
//    public ResponseEntity<List<OrderPackageResponse>> getAllPackages(@ModelAttribute PackageSearchParams searchParams) {
//        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//        var keycloakId = Utils.extractIdFromToken(token);
//
//        List<OrderPackageResponse> orderPackageResponses = packageService.getAllPackages(searchParams,keycloakId);
//
//        return ResponseEntity.ok(orderPackageResponses);
//    }
//
//    @GetMapping("/{orderCode}")
//    public ResponseEntity<OrderPackageDetailResponse> getPackageById(@PathVariable String orderCode) {
//        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//        var keycloakId = Utils.extractIdFromToken(token);
//
//        OrderPackageDetailResponse orderPackageResponses = packageService.getPackageByOrderId(orderCode, keycloakId);
//
//        return ResponseEntity.ok(orderPackageResponses);
//    }
}
