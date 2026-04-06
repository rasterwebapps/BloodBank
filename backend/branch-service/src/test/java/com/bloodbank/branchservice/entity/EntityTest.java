package com.bloodbank.branchservice.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    // ── BaseEntity inherited fields helper ──────────────────────────────

    private void assertBaseEntityFields(Object entity) throws Exception {
        var cls = entity.getClass();

        // id
        UUID id = UUID.randomUUID();
        cls.getMethod("setId", UUID.class).invoke(entity, id);
        assertEquals(id, cls.getMethod("getId").invoke(entity));

        // createdAt
        LocalDateTime now = LocalDateTime.now();
        cls.getMethod("setCreatedAt", LocalDateTime.class).invoke(entity, now);
        assertEquals(now, cls.getMethod("getCreatedAt").invoke(entity));

        // updatedAt
        LocalDateTime updated = LocalDateTime.now().plusHours(1);
        cls.getMethod("setUpdatedAt", LocalDateTime.class).invoke(entity, updated);
        assertEquals(updated, cls.getMethod("getUpdatedAt").invoke(entity));

        // createdBy
        cls.getMethod("setCreatedBy", String.class).invoke(entity, "user1");
        assertEquals("user1", cls.getMethod("getCreatedBy").invoke(entity));

        // updatedBy
        cls.getMethod("setUpdatedBy", String.class).invoke(entity, "user2");
        assertEquals("user2", cls.getMethod("getUpdatedBy").invoke(entity));

        // version
        cls.getMethod("setVersion", Long.class).invoke(entity, 5L);
        assertEquals(5L, cls.getMethod("getVersion").invoke(entity));
    }

    // ── 1. Branch ───────────────────────────────────────────────────────

    @Test
    void testBranchConstructorAndGettersSetters() throws Exception {
        Branch branch = new Branch("BR001", "Main Branch", "MAIN", "123 Main St");

        assertEquals("BR001", branch.getBranchCode());
        assertEquals("Main Branch", branch.getBranchName());
        assertEquals("MAIN", branch.getBranchType());
        assertEquals("123 Main St", branch.getAddressLine1());

        branch.setBranchCode("BR002");
        assertEquals("BR002", branch.getBranchCode());

        branch.setBranchName("Secondary");
        assertEquals("Secondary", branch.getBranchName());

        branch.setBranchType("SATELLITE");
        assertEquals("SATELLITE", branch.getBranchType());

        branch.setAddressLine1("456 Oak Ave");
        assertEquals("456 Oak Ave", branch.getAddressLine1());

        branch.setAddressLine2("Suite 100");
        assertEquals("Suite 100", branch.getAddressLine2());

        Country country = new Country("US", "United States");
        Region region = new Region(country, "CA", "California");
        City city = new City(region, "Los Angeles");
        branch.setCity(city);
        assertEquals(city, branch.getCity());

        branch.setPostalCode("90001");
        assertEquals("90001", branch.getPostalCode());

        branch.setPhone("+1234567890");
        assertEquals("+1234567890", branch.getPhone());

        branch.setEmail("branch@test.com");
        assertEquals("branch@test.com", branch.getEmail());

        branch.setLicenseNumber("LIC-001");
        assertEquals("LIC-001", branch.getLicenseNumber());

        LocalDate expiry = LocalDate.of(2025, 12, 31);
        branch.setLicenseExpiry(expiry);
        assertEquals(expiry, branch.getLicenseExpiry());

        BigDecimal lat = new BigDecimal("34.0522");
        branch.setLatitude(lat);
        assertEquals(lat, branch.getLatitude());

        BigDecimal lon = new BigDecimal("-118.2437");
        branch.setLongitude(lon);
        assertEquals(lon, branch.getLongitude());

        branch.setStatus("ACTIVE");
        assertEquals("ACTIVE", branch.getStatus());

        Branch parent = new Branch("BR000", "HQ", "HQ", "HQ Address");
        branch.setParentBranch(parent);
        assertEquals(parent, branch.getParentBranch());

        List<BranchOperatingHours> hours = new ArrayList<>();
        branch.setOperatingHours(hours);
        assertEquals(hours, branch.getOperatingHours());

        List<BranchEquipment> equip = new ArrayList<>();
        branch.setEquipment(equip);
        assertEquals(equip, branch.getEquipment());

        List<BranchRegion> regions = new ArrayList<>();
        branch.setBranchRegions(regions);
        assertEquals(regions, branch.getBranchRegions());

        assertBaseEntityFields(branch);
    }

    // ── 2. BranchOperatingHours ─────────────────────────────────────────

    @Test
    void testBranchOperatingHoursConstructorAndGettersSetters() throws Exception {
        Branch branch = new Branch("BR001", "Branch", "MAIN", "Addr");
        LocalTime open = LocalTime.of(8, 0);
        LocalTime close = LocalTime.of(17, 0);
        BranchOperatingHours hours = new BranchOperatingHours(branch, "MONDAY", open, close);

        assertEquals(branch, hours.getBranch());
        assertEquals("MONDAY", hours.getDayOfWeek());
        assertEquals(open, hours.getOpenTime());
        assertEquals(close, hours.getCloseTime());

        Branch newBranch = new Branch("BR002", "Other", "SAT", "Addr2");
        hours.setBranch(newBranch);
        assertEquals(newBranch, hours.getBranch());

        hours.setDayOfWeek("TUESDAY");
        assertEquals("TUESDAY", hours.getDayOfWeek());

        LocalTime newOpen = LocalTime.of(9, 0);
        hours.setOpenTime(newOpen);
        assertEquals(newOpen, hours.getOpenTime());

        LocalTime newClose = LocalTime.of(18, 0);
        hours.setCloseTime(newClose);
        assertEquals(newClose, hours.getCloseTime());

        hours.setClosed(true);
        assertTrue(hours.isClosed());
        hours.setClosed(false);
        assertFalse(hours.isClosed());

        assertBaseEntityFields(hours);
    }

    // ── 3. BranchEquipment ──────────────────────────────────────────────

    @Test
    void testBranchEquipmentConstructorAndGettersSetters() throws Exception {
        Branch branch = new Branch("BR001", "Branch", "MAIN", "Addr");
        BranchEquipment eq = new BranchEquipment(branch, "Centrifuge", "LAB");

        assertEquals(branch, eq.getBranch());
        assertEquals("Centrifuge", eq.getEquipmentName());
        assertEquals("LAB", eq.getEquipmentType());

        Branch newBranch = new Branch("BR002", "Other", "SAT", "Addr2");
        eq.setBranch(newBranch);
        assertEquals(newBranch, eq.getBranch());

        eq.setEquipmentName("Refrigerator");
        assertEquals("Refrigerator", eq.getEquipmentName());

        eq.setEquipmentType("STORAGE");
        assertEquals("STORAGE", eq.getEquipmentType());

        eq.setSerialNumber("SN-12345");
        assertEquals("SN-12345", eq.getSerialNumber());

        eq.setManufacturer("Thermo Fisher");
        assertEquals("Thermo Fisher", eq.getManufacturer());

        eq.setModel("Model X");
        assertEquals("Model X", eq.getModel());

        LocalDate purchase = LocalDate.of(2023, 1, 15);
        eq.setPurchaseDate(purchase);
        assertEquals(purchase, eq.getPurchaseDate());

        LocalDate lastMaint = LocalDate.of(2024, 6, 1);
        eq.setLastMaintenanceDate(lastMaint);
        assertEquals(lastMaint, eq.getLastMaintenanceDate());

        LocalDate nextMaint = LocalDate.of(2025, 6, 1);
        eq.setNextMaintenanceDate(nextMaint);
        assertEquals(nextMaint, eq.getNextMaintenanceDate());

        eq.setStatus("OPERATIONAL");
        assertEquals("OPERATIONAL", eq.getStatus());

        assertBaseEntityFields(eq);
    }

    // ── 4. BranchRegion ─────────────────────────────────────────────────

    @Test
    void testBranchRegionConstructorAndGettersSetters() throws Exception {
        Branch branch = new Branch("BR001", "Branch", "MAIN", "Addr");
        Country country = new Country("US", "United States");
        Region region = new Region(country, "CA", "California");
        BranchRegion br = new BranchRegion(branch, region);

        assertEquals(branch, br.getBranch());
        assertEquals(region, br.getRegion());

        Branch newBranch = new Branch("BR002", "Other", "SAT", "Addr2");
        br.setBranch(newBranch);
        assertEquals(newBranch, br.getBranch());

        Region newRegion = new Region(country, "NY", "New York");
        br.setRegion(newRegion);
        assertEquals(newRegion, br.getRegion());

        br.setPrimary(true);
        assertTrue(br.isPrimary());
        br.setPrimary(false);
        assertFalse(br.isPrimary());

        assertBaseEntityFields(br);
    }

    // ── 5. BloodGroup ───────────────────────────────────────────────────

    @Test
    void testBloodGroupConstructorAndGettersSetters() throws Exception {
        BloodGroup bg = new BloodGroup("A_POSITIVE", "A+ blood group");

        assertEquals("A_POSITIVE", bg.getGroupName());
        assertEquals("A+ blood group", bg.getDescription());

        bg.setGroupName("O_NEGATIVE");
        assertEquals("O_NEGATIVE", bg.getGroupName());

        bg.setDescription("Universal donor");
        assertEquals("Universal donor", bg.getDescription());

        bg.setActive(true);
        assertTrue(bg.isActive());
        bg.setActive(false);
        assertFalse(bg.isActive());

        assertBaseEntityFields(bg);
    }

    // ── 6. ComponentType ────────────────────────────────────────────────

    @Test
    void testComponentTypeConstructorAndGettersSetters() throws Exception {
        ComponentType ct = new ComponentType("RBC", "Red Blood Cells", 42);

        assertEquals("RBC", ct.getTypeCode());
        assertEquals("Red Blood Cells", ct.getTypeName());
        assertEquals(42, ct.getShelfLifeDays());

        ct.setTypeCode("PLT");
        assertEquals("PLT", ct.getTypeCode());

        ct.setTypeName("Platelets");
        assertEquals("Platelets", ct.getTypeName());

        ct.setDescription("Platelet concentrate");
        assertEquals("Platelet concentrate", ct.getDescription());

        ct.setShelfLifeDays(5);
        assertEquals(5, ct.getShelfLifeDays());

        BigDecimal min = new BigDecimal("2.0");
        ct.setStorageTempMin(min);
        assertEquals(min, ct.getStorageTempMin());

        BigDecimal max = new BigDecimal("6.0");
        ct.setStorageTempMax(max);
        assertEquals(max, ct.getStorageTempMax());

        ct.setActive(true);
        assertTrue(ct.isActive());
        ct.setActive(false);
        assertFalse(ct.isActive());

        assertBaseEntityFields(ct);
    }

    // ── 7. Country ──────────────────────────────────────────────────────

    @Test
    void testCountryConstructorAndGettersSetters() throws Exception {
        Country c = new Country("US", "United States");

        assertEquals("US", c.getCountryCode());
        assertEquals("United States", c.getCountryName());

        c.setCountryCode("CA");
        assertEquals("CA", c.getCountryCode());

        c.setCountryName("Canada");
        assertEquals("Canada", c.getCountryName());

        c.setPhoneCode("+1");
        assertEquals("+1", c.getPhoneCode());

        c.setActive(true);
        assertTrue(c.isActive());
        c.setActive(false);
        assertFalse(c.isActive());

        List<Region> regions = new ArrayList<>();
        c.setRegions(regions);
        assertEquals(regions, c.getRegions());

        assertBaseEntityFields(c);
    }

    // ── 8. Region ───────────────────────────────────────────────────────

    @Test
    void testRegionConstructorAndGettersSetters() throws Exception {
        Country country = new Country("US", "United States");
        Region r = new Region(country, "CA", "California");

        assertEquals(country, r.getCountry());
        assertEquals("CA", r.getRegionCode());
        assertEquals("California", r.getRegionName());

        Country newCountry = new Country("CA", "Canada");
        r.setCountry(newCountry);
        assertEquals(newCountry, r.getCountry());

        r.setRegionCode("ON");
        assertEquals("ON", r.getRegionCode());

        r.setRegionName("Ontario");
        assertEquals("Ontario", r.getRegionName());

        r.setActive(true);
        assertTrue(r.isActive());
        r.setActive(false);
        assertFalse(r.isActive());

        List<City> cities = new ArrayList<>();
        r.setCities(cities);
        assertEquals(cities, r.getCities());

        assertBaseEntityFields(r);
    }

    // ── 9. City ─────────────────────────────────────────────────────────

    @Test
    void testCityConstructorAndGettersSetters() throws Exception {
        Country country = new Country("US", "United States");
        Region region = new Region(country, "CA", "California");
        City city = new City(region, "Los Angeles");

        assertEquals(region, city.getRegion());
        assertEquals("Los Angeles", city.getCityName());

        Region newRegion = new Region(country, "NY", "New York");
        city.setRegion(newRegion);
        assertEquals(newRegion, city.getRegion());

        city.setCityName("Buffalo");
        assertEquals("Buffalo", city.getCityName());

        city.setPostalCode("90001");
        assertEquals("90001", city.getPostalCode());

        city.setActive(true);
        assertTrue(city.isActive());
        city.setActive(false);
        assertFalse(city.isActive());

        assertBaseEntityFields(city);
    }

    // ── 10. DeferralReason ──────────────────────────────────────────────

    @Test
    void testDeferralReasonConstructorAndGettersSetters() throws Exception {
        DeferralReason dr = new DeferralReason("LOW_HB", "Low hemoglobin", "TEMPORARY");

        assertEquals("LOW_HB", dr.getReasonCode());
        assertEquals("Low hemoglobin", dr.getReasonDescription());
        assertEquals("TEMPORARY", dr.getDeferralType());

        dr.setReasonCode("TATTOO");
        assertEquals("TATTOO", dr.getReasonCode());

        dr.setReasonDescription("Recent tattoo");
        assertEquals("Recent tattoo", dr.getReasonDescription());

        dr.setDeferralType("PERMANENT");
        assertEquals("PERMANENT", dr.getDeferralType());

        dr.setDefaultDays(180);
        assertEquals(180, dr.getDefaultDays());

        dr.setActive(true);
        assertTrue(dr.isActive());
        dr.setActive(false);
        assertFalse(dr.isActive());

        assertBaseEntityFields(dr);
    }

    // ── 11. ReactionType ────────────────────────────────────────────────

    @Test
    void testReactionTypeConstructorAndGettersSetters() throws Exception {
        ReactionType rt = new ReactionType("FEBRILE", "Febrile Reaction", "MILD");

        assertEquals("FEBRILE", rt.getReactionCode());
        assertEquals("Febrile Reaction", rt.getReactionName());
        assertEquals("MILD", rt.getSeverity());

        rt.setReactionCode("ALLERGIC");
        assertEquals("ALLERGIC", rt.getReactionCode());

        rt.setReactionName("Allergic Reaction");
        assertEquals("Allergic Reaction", rt.getReactionName());

        rt.setSeverity("SEVERE");
        assertEquals("SEVERE", rt.getSeverity());

        rt.setDescription("Severe allergic reaction");
        assertEquals("Severe allergic reaction", rt.getDescription());

        rt.setActive(true);
        assertTrue(rt.isActive());
        rt.setActive(false);
        assertFalse(rt.isActive());

        assertBaseEntityFields(rt);
    }

    // ── 12. IcdCode ─────────────────────────────────────────────────────

    @Test
    void testIcdCodeConstructorAndGettersSetters() throws Exception {
        IcdCode ic = new IcdCode("D50.0", "Iron deficiency anemia");

        assertEquals("D50.0", ic.getIcdCode());
        assertEquals("Iron deficiency anemia", ic.getDescription());

        ic.setIcdCode("D51.0");
        assertEquals("D51.0", ic.getIcdCode());

        ic.setDescription("Vitamin B12 deficiency anemia");
        assertEquals("Vitamin B12 deficiency anemia", ic.getDescription());

        ic.setCategory("ANEMIA");
        assertEquals("ANEMIA", ic.getCategory());

        ic.setActive(true);
        assertTrue(ic.isActive());
        ic.setActive(false);
        assertFalse(ic.isActive());

        assertBaseEntityFields(ic);
    }
}
