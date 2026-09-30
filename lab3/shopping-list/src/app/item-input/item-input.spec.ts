import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ItemInput } from './item-input';

describe('ItemInput', () => {
  let component: ItemInput;
  let fixture: ComponentFixture<ItemInput>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItemInput],
    }).compileComponents();

    fixture = TestBed.createComponent(ItemInput);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit a non-empty item and clear the input', () => {
    const emitSpy = vi.spyOn(component.itemAdded, 'emit');
    component.item = '  5 pommes  ';

    component.addItem();

    expect(emitSpy).toHaveBeenCalledWith('5 pommes');
    expect(component.item).toBe('');
  });
});
