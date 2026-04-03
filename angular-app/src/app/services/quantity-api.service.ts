import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { API_BASE_URL } from '../app.constants';

import {
  CalculatorOperation,
  MeasurementRecord,
  QuantityInputPayload,
  QuantityPayload
} from '../measurement-data';

type ConvertResponse = {
  id: number;
  operation: string;
  resultValue: number;
  resultUnit: string;
};

export type CompareResponse = {
  id: number;
  operation: string;
  resultValue: number;
  resultUnit: string;
};

type QuantityResultResponse = {
  id: number;
  operation: string;
  resultValue: number;
};

@Injectable({ providedIn: 'root' })
export class QuantityApiService {

  private readonly http = inject(HttpClient);

  // ================= CONVERT =================
  convert(input: QuantityPayload, targetUnit: string): Promise<ConvertResponse> {

    const payload = {
      thisQuantityDTO: input,
      thatQuantityDTO: {
        unit: targetUnit
      }
    };

    return firstValueFrom(
      this.http.post<ConvertResponse>(
        `${API_BASE_URL}/api/v1/quantities/convert`,
        payload
      )
    );
  }

  // ================= COMPARE =================
  compare(request: QuantityInputPayload): Promise<CompareResponse> {
    return firstValueFrom(
      this.http.post<CompareResponse>(
        `${API_BASE_URL}/api/v1/quantities/compare`,
        request
      )
    );
  }

  // ================= CALCULATOR =================
  calculate(
    operation: Exclude<CalculatorOperation, 'convert' | 'compare'>,
    request: QuantityInputPayload
  ): Promise<QuantityResultResponse> {

    return firstValueFrom(
      this.http.post<QuantityResultResponse>(
        `${API_BASE_URL}/api/v1/quantities/${operation}`,
        request
      )
    );
  }

  // ================= GET HISTORY =================
  getMeasurements(operation?: string, type?: string): Promise<any[]> {

    const params: any = {};

    if (operation) params.operation = operation;
    if (type) params.type = type;

    return firstValueFrom(
      this.http.get<any[]>(
        `${API_BASE_URL}/api/v1/quantities/all`,
        { params }
      )
    );
  }

  // ================= DELETE SINGLE =================
 delete(id: number) {
  return firstValueFrom(
    this.http.delete(
      `${API_BASE_URL}/api/v1/quantities/${id}`
    )
  );
}
  loadData(): import("rxjs/internal/firstValueFrom").FirstValueFromConfig<any> {
    throw new Error('Method not implemented.');
  }
  // ================= DELETE ALL =================
  deleteAll() {
    return firstValueFrom(
      this.http.delete(
        `${API_BASE_URL}/api/v1/quantities/delete-all`
      )
    );
  }

  // ================= DELETE FILTERED =================
  deleteFiltered(operation?: string, type?: string) {

    const params: any = {};

    if (operation) params.operation = operation;
    if (type) params.type = type;

    return firstValueFrom(
      this.http.delete(
        `${API_BASE_URL}/api/v1/quantities/delete-filtered`,
        { params }
      )
    );
  }
  

}