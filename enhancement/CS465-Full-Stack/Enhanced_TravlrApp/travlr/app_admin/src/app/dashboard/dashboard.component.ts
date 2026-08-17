import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';

import { DashboardDataService } from '../services/dashboard-data.service';
import {
  RevenueByTrip,
  MonthlyRevenue,
  TopTrip,
  CancellationSummary
} from '../models/dashboard';

/**
 *  Chart.js Dashboard Components. No Controllers, or elements. Calling 
 *  registerables once at module scope enables every chart type used below.
 * 
 * Date: 08/02/2026
 * Author: Dylan Harmon
 */

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})

/**
 *  DashboardComponent
 * 
 * Renders four Chart.js visualizations over the booking aggregation endpoints.
 * Each chart is driven by a seperate pipeline.
 */
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {

  /**
   *  Canvas Element
   * 
   * ViewChild queries the component's own template rather than the global
   * document. This keeps the charts setup within Angular's rendering model.
   */
  @ViewChild('revenueCanvas') revenueCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('monthlyCanvas') monthlyCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('topTripsCanvas') topTripsCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('statusCanvas') statusCanvas!: ElementRef<HTMLCanvasElement>;

  // Every Instance is reachable in order to be destroyed before redraw
  private revenueChart?: Chart;
  private monthlyChart?: Chart;
  private topTripsChart?: Chart;
  private statusChart?: Chart;

  /**
   *  Response Data. Held on component because the template reads the summary
   *  values directly for the metric cards.
   */
  revenueData: RevenueByTrip[] = [];
  monthlyData: MonthlyRevenue[] = [];
  topTripsData: TopTrip[] = [];
  summary?: CancellationSummary;

  private viewReady = false;

  message: string = '';

  constructor(private dashboardService: DashboardDataService) { }

  /**
   *  Issue all four requests on init.
   */
  ngOnInit(): void { 
    this.loadRevenueByTrip();
    this.loadMonthlyRevenue();
    this.loadTopTrips();
    this.loadCancellationSummary();
  }

  /**
   *  Marks the view as available
   */
  ngAfterViewInit(): void {
    this.viewReady = true;

    if (this.revenueData.length) { this.drawRevenueChart(); }
    if (this.monthlyData.length) { this.drawMonthlyChart(); }
    if (this.topTripsData.length){ this.drawTopTripsChart(); }
    if (this.summary) { this.drawStatusChart(); }
  }

  /**
   *  Releases every chart instance when the component is destroyed
   */
  ngOnDestroy(): void {
    this.revenueChart?.destroy();
    this.monthlyChart?.destroy();
    this.topTripsChart?.destroy();
    this.statusChart?.destroy();
  }

  /**
   * Data Loading
   * 
   * Each handler stores its response, then draws only if the view is
   * ready.
   */
  private loadRevenueByTrip(): void {
    this.dashboardService.getRevenueByTrip().subscribe({
      next: (data) => {
        this.revenueData = data;
        if (this.viewReady) { this.drawRevenueChart(); }
      },
      error: (err) => {
        this.message = 'Unable to load revenue data.';
        console.error(err);
      }
    });
  }

  private loadMonthlyRevenue(): void {
    this.dashboardService.getMonthlyRevenue().subscribe({
      next: (data) => {
        this.monthlyData = data;
        if (this.viewReady) { this.drawMonthlyChart(); }
      },
      error: (err) => {
        this.message = 'Unable to load monthly revenue data.';
        console.error(err);
      }
    });
  }

  private loadTopTrips(): void {
    this.dashboardService.getTopTrips().subscribe({
      next: (data) => {
        this.topTripsData = data;
        if (this.viewReady) { this.drawTopTripsChart(); }
      },
      error: (err) => {
        this.message = 'Unable to load top trip data.';
        console.error(err);
      }
    });
  }

  private loadCancellationSummary(): void {
    this.dashboardService.getCancellationSummary().subscribe({
      next: (data) => {
        this.summary = data;
        if (this.viewReady) { this.drawStatusChart(); }
      },
      error: (err) => {
        this.message = 'Unable to load booking status data.';
        console.error(err);
      }
    });
  }

  /**
   * Chart Consturction
   */

  // Vertical Bar for total revenue
  private drawRevenueChart(): void {
    if (!this.revenueCanvas) { return; }

    // Discard any prior instance bound to this canvas
    this.revenueChart?.destroy();

    this.revenueChart = new Chart(this.revenueCanvas.nativeElement, {
      type: 'bar',
      data: {
        labels: this.revenueData.map(t => t.tripName),
        datasets: [{
          label: 'Total Revenue (USD)',
          data: this.revenueData.map(t => t.totalRevenue),
          backgroundColor: '#2c7873',
          borderColor: '#1b4f4b',
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false}
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              // Abbreviate to thousands
              callback: (value) => '$' + (Number(value) / 1000) + 'k'
            }
          }
        }
      }
    });
  }

  // Line chart of revenue
  private drawMonthlyChart(): void {
    if (!this.monthlyCanvas) { return; }

    this.monthlyChart?.destroy();

    this.monthlyChart = new Chart(this.monthlyCanvas.nativeElement, {
      type: 'line',
      data: {
        labels: this.monthlyData.map(m => m.month),
        datasets: [{
          label: 'Revenue (USD)',
          data: this.monthlyData.map(m => m.revenue),
          borderColor: '#c0562d',
          backgroundColor: 'rgba(192, 86, 45, 0.15)',
          fill: true,

          tension: 0.3,
          pointRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value) => '$' + (Number(value) / 1000) + 'k'
            }
          }
        }
      }
    });
  }

  // Horizontal Bar Chart (Top Five)
  private drawTopTripsChart(): void {
    if (!this.topTripsCanvas) { return; }

    this.topTripsChart?.destroy();

    this.topTripsChart = new Chart(this.topTripsCanvas.nativeElement, {
      type: 'bar',
      data: {
        labels: this.topTripsData.map(t => t.tripName),
        datasets: [{
          label: 'Bookings',
          data: this.topTripsData.map(t => t.bookingCount),
          backgroundColor: '#3d6b9c',
          borderColor: '#26456b',
          borderWidth: 1
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          x: { beginAtZero: true }
        }
      }
    });
  }

  // Doughnut Chart of booking counts by status.
  private drawStatusChart(): void {
    if (!this.statusCanvas || !this.summary) { return; }

    this.statusChart?.destroy();

    this.statusChart = new Chart(this.statusCanvas.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Completed', 'Confirmed', 'Cancelled'],
        datasets: [{
          data: [
            this.summary.completed,
            this.summary.confirmed,
            this.summary.cancelled
          ],
          backgroundColor: ['#2c7873', '#3d6b9c', '#a33b3b'],
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom' }
        }
      }
    });
  }
  
}

