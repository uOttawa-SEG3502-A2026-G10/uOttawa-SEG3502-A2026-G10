import {Component} from '@angular/core';
import {FormsModule} from "@angular/forms";

@Component({
    selector: 'app-calculator',
    templateUrl: './calculator.html',
    styleUrls: ['./calculator.css'],
    standalone: true,
  imports: [FormsModule]
})
export class Calculator {
    firstNum = 0;
    secondNum = 0;

    result = 0;

    sum(): void {
      this.castValues();
      this.result = this.firstNum + this.secondNum;
    }

    subtract(): void {
      this.castValues();
      this.result = this.firstNum - this.secondNum;
    }

    multiply(): void {
      this.castValues();
      this.result = this.firstNum * this.secondNum;
    }

    divide(): void {
      this.castValues();
      this.result = this.firstNum / this.secondNum;
    }



    private castValues(): void {
      this.firstNum = Number(this.firstNum);
      this.secondNum = Number(this.secondNum);
    }
}