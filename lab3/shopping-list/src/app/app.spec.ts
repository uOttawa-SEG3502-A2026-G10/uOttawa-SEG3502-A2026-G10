import { TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
    })
      .compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should add and remove an item', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;

    app.addItem('5 pommes');
    expect(app.items).toEqual(['5 pommes']);

    app.removeItem(0);
    expect(app.items).toEqual([]);
  });
});
