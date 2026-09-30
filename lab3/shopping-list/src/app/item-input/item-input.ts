import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  imports: [FormsModule],
  selector: 'app-item-input',
  styleUrl: './item-input.css',
  templateUrl: './item-input.html',
})
export class ItemInput {
  @Output() itemAdded = new EventEmitter<string>();

  item = '';

  addItem(): void {
    const item = this.item.trim();

    if (!item) {
      return;
    }

    this.itemAdded.emit(item);
    this.item = '';
  }
}
