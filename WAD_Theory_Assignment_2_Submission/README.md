# WAD Theory Assignment 2 - Submission Package
**Course:** ICS1503 - Web Application Development (Theory)  
**Institution:** Sri Sivasubramaniya Nadar College of Engineering  
**Department:** Computer Science and Engineering  
**Title:** Design and Implementation of Test Cases for a Spring Boot Application  
**Student:** Vijayaraaghavan K S  
**Batch:** 2024 - 2029 (5-Year Integrated M.Tech CSE)  

---

## Submission Contents

This folder contains all artifacts required by the Theory Assignment 2 rubric:

```
WAD_Theory_Assignment_2_Submission/
├── Documentation_Report.pdf                  # Compiled formal report generated via Tectonic
├── Documentation_Report.tex                  # Full LaTeX source code
├── README.md                                 # This submission documentation file
│
├── Spring_Boot_Test_Files/                   # Spring Boot Context & Integration Tests
│   ├── CartBackendApplicationTests.java      # Monolith @SpringBootTest Context test
│   ├── CartServiceApplicationTests.java      # Microservice Cart @SpringBootTest test
│   └── ProductServiceApplicationTests.java   # Microservice Product @SpringBootTest test
│
├── JUnit_Test_Cases/                         # JUnit 5 & Mockito Unit Tests (Business Layer)
│   ├── CartServiceTest_Monolith.java         # 6 Unit tests (merge rules, duplicate handling, total calculation)
│   └── CartServiceTest_Microservice.java     # 2 Unit tests (inter-service RestTemplate mocking)
│
├── MockMvc_Test_Cases/                       # MockMvc Controller Tests (Web Layer - Positive & Negative)
│   ├── ProductControllerTest_Monolith.java   # 10 Tests:
│   │                                         #  1. getAllProducts_returnsJsonArray (Positive)
│   │                                         #  2. getProductById_validId_returnsProduct (Positive)
│   │                                         #  3. getProductById_invalidId_returnsNotFound (Negative - 404)
│   │                                         #  4. createProduct_validData_savesAndReturnsIt (Positive)
│   │                                         #  5. createProduct_missingOrEmptyName_returnsBadRequest (Negative - 400)
│   │                                         #  6. createProduct_negativePrice_returnsBadRequest (Negative - 400)
│   │                                         #  7. updateProduct_existingProduct_updatesAndReturnsIt (Positive)
│   │                                         #  8. updateProduct_nonExistentProduct_returnsNotFound (Negative - 404)
│   │                                         #  9. deleteProduct_existingProduct_returnsOk (Positive)
│   │                                         # 10. verifyDeletedProduct_cannotBeRetrieved (Negative - 404)
│   └── ProductControllerTest_Microservice.java
│
├── Test_Configuration_Files/                 # Build & Runtime Test Configs
│   ├── pom_cart_backend.xml                  # Maven dependencies (spring-boot-starter-test, webmvc-test, etc.)
│   ├── application_cart_backend.properties   # MongoDB connection & server port 8082
│   ├── application_cart_service.properties   # Cart service port 8084 & product.service.url
│   └── application_product_service.properties# Product service port 8083 & E-CommDB
│
└── Screenshots_and_Reports/                  # Evidence of Successful Execution
    ├── 01_test_execution_terminal.png        # Terminal screenshot showing 17/17 tests passing & BUILD SUCCESS
    ├── 02_test_execution_report.txt          # Full text log of the maven test run
    ├── 03_ui_shop_view.png                   # Shop view UI screenshot
    ├── 04_ui_admin_view.png                  # Admin product catalogue management UI screenshot
    └── 05_ui_developer_dashboard.png         # Developer observability dashboard with live request logs
```

---

## How to Execute the Tests

To run the complete test suite:

```bash
cd ass8/cart-backend
./mvnw test
```

### Expected Results
- **ProductControllerTest**: 10 tests run, 0 failures, 0 errors
- **CartServiceTest**: 6 tests run, 0 failures, 0 errors
- **CartBackendApplicationTests**: 1 test run, 0 failures, 0 errors
- **Total**: 17 tests run, 0 failures, 0 errors (**BUILD SUCCESS**)
