import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import{
  RevenueByTrip,
  MonthlyRevenue,
  TopTrip,
  CancellationSummary
} from '../models/dashboard';

/**
 *  DashboardDataService
 * 
 * Provides read access to the aggregation endpointws under /api/dashboard.
 * 
 * Date: 8/2/2026
 * Author: Dylan Harmon
 */
@Injectable({
  providedIn: 'root'
})
export class DashboardDataService {

  constructor(private http: HttpClient) { }

  /**
   * Base URL for the dashboard endpoints.
   */
  private baseUrl = 'http://localhost:3000/api/dashboard';

  /**
   * Total revenue and booking volume per trip, ranked by revenue
   * 
   * @return Observable emitting one entry per trip in the catalog
   */
  getRevenueByTrip(): Observable<RevenueByTrip[]> {
    return this.http.get<RevenueByTrip[]>(this.baseUrl + '/revenue-by-trip');
  }

  /**
   * Revenue and booking volume per month
   * 
   * @return Observable emitting one entry per month, oldest first
   */
  getMonthlyRevenue(): Observable<MonthlyRevenue[]> {
    return this.http.get<MonthlyRevenue[]>(this.baseUrl + '/monthly-revenue');
  }

  /**
   * Most frequently booked trips.
   * 
   * @return Observable emitting the top five most popular trips
   */
  getTopTrips(): Observable<TopTrip[]> {
    return this.http.get<TopTrip[]>(this.baseUrl + '/top-trips');
  }

  /**
   * Booking counts by status, revenue lost to cancellations, and the
   * cancellation rate.
   * 
   * @return Observable with a single summary object.
   */
  getCancellationSummary(): Observable<CancellationSummary> {
    return this.http.get<CancellationSummary>(
      this.baseUrl + '/cancellation-rate');
  }
}