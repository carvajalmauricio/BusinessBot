package com.innovaweb.businessbot;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private static List<Product> productList;

    public ProductAdapter(InventoryActivity2 inventoryActivity2, List<Product> productList) {
        this.productList = productList;
    }
    public List<Product> getProductList() {
        return productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.nameTextView.setText(product.getNombre());
        holder.descriptionTextView.setText(product.getDescripcion());
        holder.priceTextView.setText(String.valueOf(product.getPrecio()));
        holder.quantityTextView.setText(String.valueOf(product.getCantidad()));

        holder.nameTextView.removeTextChangedListener(holder.nameTextWatcher);
        holder.descriptionTextView.removeTextChangedListener(holder.descriptionWatcher);
        holder.priceTextView.removeTextChangedListener(holder.priceTextWatcher);
        holder.quantityTextView.removeTextChangedListener(holder.quantityWatcher);

        // Add new TextWatchers
        holder.nameTextView.addTextChangedListener(holder.nameTextWatcher);
        holder.descriptionTextView.addTextChangedListener(holder.descriptionWatcher);
        holder.priceTextView.addTextChangedListener(holder.priceTextWatcher);
        holder.quantityTextView.addTextChangedListener(holder.quantityWatcher);
    }

    @Override
    public int getItemCount() {

        if (productList == null){
            return 0;
        }else {
        return productList.size();}
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private EditText nameTextView,descriptionTextView, priceTextView,quantityTextView;
        TextWatcher nameTextWatcher,descriptionWatcher, priceTextWatcher,quantityWatcher;

        private Button incrementButton;
        private Button decrementButton;
        private Button deleteButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            deleteButton = itemView.findViewById(R.id.delete_button);
            nameTextView = itemView.findViewById(R.id.product_name);
            descriptionTextView = itemView.findViewById(R.id.product_description);
            priceTextView = itemView.findViewById(R.id.product_price);
            quantityTextView = itemView.findViewById(R.id.product_quantity);
            incrementButton = itemView.findViewById(R.id.increment_button);
            decrementButton = itemView.findViewById(R.id.decrement_button);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the current position of the item to be deleted
                    int position = getAdapterPosition();

                    // Check if position is not out of bounds for the recyclerView or -1
                    if (position != RecyclerView.NO_POSITION) {
                        removeAt(position);
                    }
                }
            });

            incrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the current quantity
                    int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
                    // Increment the quantity
                    currentQuantity++;
                    // Update the quantityTextView
                    quantityTextView.setText(String.valueOf(currentQuantity));
                }
            });

            decrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the current quantity
                    int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());

                    // Decrement the quantity only if it's greater than 1
                    if (currentQuantity > 1) {
                        currentQuantity--;

                        // Update the quantityTextView
                        quantityTextView.setText(String.valueOf(currentQuantity));
                    }
                }
            });

            nameTextWatcher = createTextWatcher(product -> product.setNombre(nameTextView.getText().toString()));
            descriptionWatcher = createTextWatcher(product -> product.setDescripcion(descriptionTextView.getText().toString()));
            priceTextWatcher = createTextWatcher(product -> product.setPrecio(Double.parseDouble(priceTextView.getText().toString())));
            quantityWatcher = createTextWatcher(product -> product.setCantidad(Integer.parseInt(quantityTextView.getText().toString())));

        }

        public void removeAt(int position) {
            productList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, productList.size());
        }
        private TextWatcher createTextWatcher(Consumer<Product> onTextChanged) {
            return new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Product product = productList.get(getAdapterPosition());
                    onTextChanged.accept(product);
                }
                @Override
                public void afterTextChanged(Editable s) {}
            };
        }
    }


}
