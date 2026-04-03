import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { THEME_STORAGE_KEY } from '../app.constants';
import {
  MeasurementRecord,
  MeasurementType,
  formatMeasurementType,
  formatTimestamp,
  historyOperationOptions,
  measurementCatalog
} from '../measurement-data';
import { AuthService } from '../services/auth.service';
import { QuantityApiService } from '../services/quantity-api.service';

@Component({
  selector: 'app-history-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './history-page.component.html'
})
export class HistoryPageComponent {

  private readonly authService = inject(AuthService);
  private readonly quantityApi = inject(QuantityApiService);

  readonly darkMode = signal(localStorage.getItem(THEME_STORAGE_KEY) !== 'light');
  readonly user = this.authService.user;
  readonly operationOptions = historyOperationOptions;
  readonly typeOptions = Object.keys(measurementCatalog) as MeasurementType[];

  loading = false;
  errorMessage = '';
  selectedOperation = '';
  selectedType = '';
  records: MeasurementRecord[] = [];

  constructor() {
    this.loadMeasurements();
  }

  toggleTheme(): void {
    const next = !this.darkMode();
    this.darkMode.set(next);
    localStorage.setItem(THEME_STORAGE_KEY, next ? 'dark' : 'light');
  }

  logout(): void {
    this.authService.logout();
  }

  // ================= LOAD =================
  async loadMeasurements() {
    this.loading = true;
    this.errorMessage = '';

    try {
      const response = await this.quantityApi.getMeasurements(
        this.selectedOperation,
        this.selectedType
      );

      this.records = (response ?? []).map((item: any) => ({
  id: item.id,
  operation: item.operation,
  operand1: `${item.thisValue} ${item.thisUnit}`,
  operand2: `${item.thatValue} ${item.thatUnit}`,
  result: item.resultValue, // ✅ only value

  // ✅ REQUIRED FIELDS (FIXED)
  error: null,
  operationType: item.operation,
  isError: false,

  measurementType: item.thisMeasurementType,
  createdAt: item.createdAt ?? null,
  updatedAt: item.updatedAt ?? null
}));

    } catch (error) {
      this.errorMessage = "Failed to load measurement history";
      this.records = [];
    } finally {
      this.loading = false;
    }
  }

  formatType(type: string | null): string {
    return formatMeasurementType(type);
  }

  formatWhen(timestamp: string | null): string {
    return formatTimestamp(timestamp);
  }

  // ================= DELETE SINGLE =================
 async deleteRecord(id: number) {
  console.log("Deleting ID:", id);   // 👈 MUST CHECK

  const confirmDelete = window.confirm("Are you sure?");
  if (!confirmDelete) return;

  try {
    await this.quantityApi.delete(id);
    await this.loadMeasurements();
  } catch (error) {
    console.error(error);
  }
}

  // ================= DELETE ALL / FILTERED =================
  async deleteAll() {

  const confirmDelete = window.confirm("Delete selected records?");
  if (!confirmDelete) return;

  try {

    if (this.selectedOperation || this.selectedType) {
      await this.quantityApi.deleteFiltered(
        this.selectedOperation,
        this.selectedType
      );
    } else {
      await this.quantityApi.deleteAll();
    }

    // ✅ IMPORTANT → reload data
    await this.loadMeasurements();

  } catch (error: any) {
    console.error("Delete all error:", error);
    this.errorMessage = "Failed to delete records";
  }
}


}