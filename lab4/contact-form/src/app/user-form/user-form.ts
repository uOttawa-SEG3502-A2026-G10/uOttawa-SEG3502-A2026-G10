import {Component, inject} from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup, FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import {ResultService} from "../result-service";

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './user-form.html',
})
export class UserForm {
  private resultService = inject(ResultService);

  userForm = new FormGroup({
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),

    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),

    phone: new FormControl('', {
      nonNullable: true,
      validators: [phoneValidator]
    }),

    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.email]
    })
  });

  constructor(private router: Router) {}

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    const user = this.userForm.getRawValue();

    // Transmettre les données à la page suivante
    this.resultService.setCurrentUser(user);
    this.router.navigate(['/result']);
  }
}

/**
 * Numéro de téléphone :
 * - optionel
 * - exactement 10 chiffres lorsqu'il est fourni
 * - premier chiffre différent de 0
 * - quatrième chiffre différent de 0
 *
 * Exemples valides :
 * 6131234567
 * 5149876543
 */
function phoneValidator(control: AbstractControl) {
  const value = control.value;

  // Le champ est optionel
  if (!value) {
    return null;
  }

  // 10 chiffres, premier et quatrième différents de 0
  let valid = true;
  if (value.length !== 10 || value[0] === '0' || value[3] === '0' || Number.isNaN(value)) {
    valid = false;
  }

  return valid ? null : { invalidPhone: true };
}