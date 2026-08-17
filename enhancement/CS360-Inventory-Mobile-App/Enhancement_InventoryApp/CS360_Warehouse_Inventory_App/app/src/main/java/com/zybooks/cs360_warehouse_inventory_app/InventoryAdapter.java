package com.zybooks.cs360_warehouse_inventory_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    // List of Inventory Items to display
    private List<InventoryItem> itemList;
    private final InventoryViewModel viewModel;

    // Payload type: used for granular ViewHolder updates
    // Prevents full rebind of entire view, change only the view needed
    public enum Payload {
        QUANTITY_CHANGE
    }

    // Public Constructor
    public InventoryAdapter(List<InventoryItem> itemList, InventoryViewModel viewModel) {

        this.itemList = itemList;
        this.viewModel = viewModel;
    }

    // Updates the list and refreshes the display
    @SuppressWarnings("unused")
    public void updateList(List<InventoryItem> newList) {
        int oldSize = this.itemList.size();
        int newSize = itemList.size();
        this.itemList = newList;
        if (newSize > oldSize) {
            notifyItemInserted(newSize - 1);
            notifyItemRangeChanged(0, oldSize);
        } else if (newSize < oldSize) {
            notifyItemRemoved(oldSize - 1);
        } else {
            notifyItemRangeChanged(0, newSize);
        }
    }

    // VIEWHOLDER
    // Holds references to the views inside one row
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView invItemName;
        TextView textViewQuantity;
        Button buttonIncreaseInventory;
        Button buttonReduceInventory;
        Button buttonDelete;

        public ViewHolder(View  itemView) {
            super(itemView);
            invItemName             = itemView.findViewById(R.id.invItemName);
            textViewQuantity        = itemView.findViewById(R.id.textViewQuantity);
            buttonIncreaseInventory = itemView.findViewById(R.id.buttonIncreaseInventory);
            buttonReduceInventory   = itemView.findViewById(R.id.buttonReduceInventory);
            buttonDelete            = itemView.findViewById(R.id.buttonDelete);
        }
    }

    // INFLATE ROW LAYOUT
    // Called once per visible row
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    // Payload Bind
    // Only called when notifyItemChanged is called with a payload
    // Only rebinds specific view instead of entier RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload == Payload.QUANTITY_CHANGE) {
                    // Only rebind the quantity number
                    holder.textViewQuantity.setText(
                            String.valueOf(itemList.get(position).getQuantity())
                    );
                }
            }
            // Once Payload handled, return
            return;
        }
        // No payload, continue to full bind
        super.onBindViewHolder(holder, position, payloads);
    }

    // Full Bind View Holder
    // Called for every row on initial load, add, and delete
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);

        // Get holder name and quantity
        holder.invItemName.setText(item.getName());
        holder.textViewQuantity.setText(String.valueOf(item.getQuantity()));

        // Increase Quantity
        holder.buttonIncreaseInventory.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            viewModel.update(item);
            notifyItemChanged(holder.getBindingAdapterPosition(), Payload.QUANTITY_CHANGE);
        });

        // Reduce quantity - stops at zero
        holder.buttonReduceInventory.setOnClickListener(v -> {
            if (item.getQuantity() > 0) {
                item.setQuantity(item.getQuantity() - 1);
                viewModel.update(item);
                notifyItemChanged(holder.getBindingAdapterPosition(), Payload.QUANTITY_CHANGE);
            }
        });

        //  Delete Item From DB
        holder.buttonDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_ID) {
                viewModel.delete(itemList.get(pos));
            }
        });
    }

    // Tells the RecyclerView how many rows to create
    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
