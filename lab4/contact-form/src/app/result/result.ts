import {Component, inject} from '@angular/core';
import {ResultService} from "../result-service";
import {UserEntity} from "../model/user-entity";


@Component({
  selector: 'app-result',
  standalone: true,
  templateUrl: './result.html'
})
export class Result {
  private resultService = inject(ResultService);
  user: UserEntity;

  constructor() {
    this.user = this.resultService.getCurrentUser()!;
  }
}