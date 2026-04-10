/**
 * Dashboard KPI summary data returned by the backend.
 */
export interface KpiData {
  totalDonorsToday: number;
  totalDonorsMonth: number;
  collectionsToday: number;
  pendingTestOrders: number;
  expiringUnits: number;
  openHospitalRequests: number;
  availableUnitsByGroup: Record<string, number>;
}

/**
 * Blood stock level per group.
 */
export interface StockLevel {
  bloodGroup: string;
  available: number;
  reserved: number;
}

/**
 * Collection workflow status breakdown.
 */
export interface CollectionStats {
  completed: number;
  inProgress: number;
  scheduled: number;
  cancelled: number;
}

/**
 * Single data point for the donation trend line chart.
 */
export interface DonationTrend {
  date: string;
  count: number;
}
