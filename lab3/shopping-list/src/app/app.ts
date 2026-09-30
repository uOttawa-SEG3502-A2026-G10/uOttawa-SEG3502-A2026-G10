import { Component } from '@angular/core';
import { ItemInput } from './item-input/item-input';
import { ShoppingList } from './shopping-list/shopping-list';

@Component({
  imports: [ItemInput, ShoppingList],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  items: string[] = [];

  addItem(item: string): void {
    this.items = [...this.items, item];
  }

  removeItem(index: number): void {
    this.items = this.items.filter((_, itemIndex) => itemIndex !== index);
  }
}
