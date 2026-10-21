import { Injectable, inject } from '@angular/core';
import {
  addDoc,
  collection,
  collectionData,
  Firestore
} from '@angular/fire/firestore';
import { Timestamp } from 'firebase/firestore';
import { map, Observable } from 'rxjs';
import { Employee } from '../model/employee';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private readonly firestore = inject(Firestore);

  // Keep one stable observable instance; the list template uses it with AsyncPipe.
  readonly $: Observable<readonly Employee[]> = collectionData(
    collection(this.firestore, 'employees')
  ).pipe(
    map(records => records.map(record => {
      const storedDate = record['dateOfBirth'];
      const dateOfBirth = storedDate instanceof Timestamp
        ? storedDate.toDate()
        : storedDate instanceof Date
          ? storedDate
          : new Date(storedDate as string | number);
      return { ...record, dateOfBirth } as Employee;
    }))
  );

  addEmployee(employee: Employee) {
    const employees = collection(this.firestore, 'employees');
    return addDoc(employees, { ...employee });
  }
}
