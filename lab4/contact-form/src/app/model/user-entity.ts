export class UserEntity {
    constructor(
        public firstName: string,
        public lastName: string,
        public phone?: string,
        public email?: string,
    ) {}
}
