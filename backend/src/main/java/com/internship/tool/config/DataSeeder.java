package com.internship.tool.config;

import com.internship.tool.entity.ConsentRecord;
import com.internship.tool.repository.ConsentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ConsentRecordRepository consentRecordRepository;

    @Override
    public void run(String... args) {
        if (consentRecordRepository.count() > 0) return;

        List<ConsentRecord> records = List.of(
            build("CUST-001", "Rahul Sharma", "rahul@gmail.com",
                "ORG-101", "HDFC Bank",
                "Credit score assessment and loan processing",
                "Financial data, Identity documents", "GRANTED"),

            build("CUST-002", "Priya Patel", "priya@gmail.com",
                "ORG-102", "Ola Cabs",
                "Location tracking for ride services",
                "Location data, Contact details", "GRANTED"),

            build("CUST-003", "Amit Kumar", "amit@gmail.com",
                "ORG-103", "Paytm",
                "Payment processing and fraud detection",
                "Financial data, Transaction history", "PENDING"),

            build("CUST-004", "Sneha Reddy", "sneha@gmail.com",
                "ORG-104", "Amazon India",
                "Personalized recommendations and purchase history",
                "Purchase history, Browsing data", "REVOKED"),

            build("CUST-005", "Vikram Singh", "vikram@gmail.com",
                "ORG-105", "PhonePe",
                "UPI payment processing",
                "Bank account details, Transaction data", "GRANTED"),

            build("CUST-006", "Anjali Nair", "anjali@gmail.com",
                "ORG-106", "Zomato",
                "Food delivery and location services",
                "Location data, Order history", "GRANTED"),

            build("CUST-007", "Ravi Verma", "ravi@gmail.com",
                "ORG-107", "Flipkart",
                "E-commerce purchase and delivery tracking",
                "Contact details, Purchase history", "EXPIRED"),

            build("CUST-008", "Deepa Iyer", "deepa@gmail.com",
                "ORG-108", "Apollo Hospitals",
                "Medical records and appointment management",
                "Health data, Identity documents", "GRANTED"),

            build("CUST-009", "Suresh Rao", "suresh@gmail.com",
                "ORG-109", "ICICI Bank",
                "Home loan application processing",
                "Financial data, Property documents", "PENDING"),

            build("CUST-010", "Meena Joshi", "meena@gmail.com",
                "ORG-110", "Airtel",
                "Telecom services and usage analytics",
                "Contact details, Usage data", "GRANTED"),

            build("CUST-011", "Arjun Das", "arjun@gmail.com",
                "ORG-101", "HDFC Bank",
                "Credit card application",
                "Financial data, Employment details", "REVOKED"),

            build("CUST-012", "Kavya Menon", "kavya@gmail.com",
                "ORG-111", "MakeMyTrip",
                "Travel booking and itinerary management",
                "Identity documents, Travel history", "GRANTED"),

            build("CUST-013", "Naveen Pillai", "naveen@gmail.com",
                "ORG-112", "Swiggy",
                "Food delivery location and preferences",
                "Location data, Dietary preferences", "PENDING"),

            build("CUST-014", "Pooja Sharma", "pooja@gmail.com",
                "ORG-113", "BYJU's",
                "Educational content personalization",
                "Academic data, Usage patterns", "GRANTED"),

            build("CUST-015", "Rohit Gupta", "rohit@gmail.com",
                "ORG-114", "Reliance Jio",
                "Telecom and digital services",
                "Contact details, Usage data", "EXPIRED"),

            build("CUST-016", "Divya Krishnan", "divya@gmail.com",
                "ORG-115", "SBI Bank",
                "Savings account and KYC verification",
                "Identity documents, Financial data", "GRANTED"),

            build("CUST-017", "Manoj Tiwari", "manoj@gmail.com",
                "ORG-116", "Uber India",
                "Ride tracking and payment processing",
                "Location data, Payment details", "GRANTED"),

            build("CUST-018", "Lakshmi Venkat", "lakshmi@gmail.com",
                "ORG-117", "Practo",
                "Online doctor consultation and health records",
                "Health data, Contact details", "PENDING"),

            build("CUST-019", "Sanjay Mehta", "sanjay@gmail.com",
                "ORG-118", "Nykaa",
                "Beauty product recommendations and purchase tracking",
                "Purchase history, Browsing data", "GRANTED"),

            build("CUST-020", "Anita Desai", "anita@gmail.com",
                "ORG-119", "PolicyBazaar",
                "Insurance policy comparison and purchase",
                "Financial data, Health data", "REVOKED"),

            build("CUST-021", "Kiran Bhat", "kiran@gmail.com",
                "ORG-120", "Myntra",
                "Fashion recommendations and order tracking",
                "Purchase history, Size preferences", "GRANTED"),

            build("CUST-022", "Rekha Pillai", "rekha@gmail.com",
                "ORG-121", "Axis Bank",
                "Personal loan processing",
                "Financial data, Employment details", "PENDING"),

            build("CUST-023", "Gopal Krishnamurthy", "gopal@gmail.com",
                "ORG-122", "Vodafone Idea",
                "Mobile services and data usage analytics",
                "Contact details, Usage data", "EXPIRED"),

            build("CUST-024", "Sunita Agarwal", "sunita@gmail.com",
                "ORG-123", "Tata 1mg",
                "Medicine delivery and health tracking",
                "Health data, Prescription data", "GRANTED"),

            build("CUST-025", "Prasad Naidu", "prasad@gmail.com",
                "ORG-124", "Groww",
                "Investment portfolio management",
                "Financial data, PAN details", "GRANTED"),

            build("CUST-026", "Meghna Jain", "meghna@gmail.com",
                "ORG-125", "Zepto",
                "Grocery delivery and location services",
                "Location data, Purchase history", "PENDING"),

            build("CUST-027", "Arun Nambiar", "arun@gmail.com",
                "ORG-126", "BSNL",
                "Broadband services and usage monitoring",
                "Contact details, Usage data", "REVOKED"),

            build("CUST-028", "Padma Subramaniam", "padma@gmail.com",
                "ORG-127", "Fortis Healthcare",
                "Hospital admission and medical records",
                "Health data, Identity documents", "GRANTED"),

            build("CUST-029", "Rohini Kulkarni", "rohini@gmail.com",
                "ORG-128", "Zerodha",
                "Stock trading and investment tracking",
                "Financial data, PAN details", "GRANTED"),

            build("CUST-030", "Vijay Anand", "vijay@gmail.com",
                "ORG-129", "BigBasket",
                "Grocery delivery and dietary preferences",
                "Location data, Purchase history", "EXPIRED")
        );

        consentRecordRepository.saveAll(records);
        System.out.println("✅ Seeded " + records.size() + " consent records");
    }

    private ConsentRecord build(
            String principalId, String principalName, String email,
            String fiduciaryId, String fiduciaryName,
            String purpose, String categories, String status) {

        return ConsentRecord.builder()
                .dataPrincipalId(principalId)
                .dataPrincipalName(principalName)
                .dataPrincipalEmail(email)
                .dataFiduciaryId(fiduciaryId)
                .dataFiduciaryName(fiduciaryName)
                .purpose(purpose)
                .dataCategories(categories)
                .consentStatus(status)
                .isActive(true)
                .isFallback(false)
                .consentDate(LocalDateTime.now().minusDays(30))
                .expiryDate(LocalDateTime.now().plusDays(335))
                .build();
    }
}