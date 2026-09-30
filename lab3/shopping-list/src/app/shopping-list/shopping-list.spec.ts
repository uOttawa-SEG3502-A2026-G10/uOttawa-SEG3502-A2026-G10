import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ShoppingList } from './shopping-list';

describe('ShoppingList', () => {
  let component: ShoppingList;
  let fixture: ComponentFixture<ShoppingList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShoppingList],
    }).compileComponents();

    fixture = TestBed.createComponent(ShoppingList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit the index of the item to delete', () => {
    const emitSpy = vi.spyOn(component.itemDeleted, 'emit');

    component.deleteItem(1);

    expect(emitSpy).toHaveBeenCalledWith(1);
  });
});
