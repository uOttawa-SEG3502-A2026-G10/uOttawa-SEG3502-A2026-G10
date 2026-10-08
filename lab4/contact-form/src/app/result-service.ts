import { Service } from '@angular/core';
import {UserEntity} from "./model/user-entity";

@Service()
export class ResultService {

    private currentUser: UserEntity | undefined = undefined;


    public getCurrentUser() {
        return this.currentUser;
    }

    public setCurrentUser(value: UserEntity) {
        this.currentUser = value;
    }
}
