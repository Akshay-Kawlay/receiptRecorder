package ak.kawlay.mail.utoronto.ca.textreader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecordViewHolder>{

    Context mContext;
    List<receiptRecord> recordList;

    public RecyclerAdapter(Context mContext, List<receiptRecord> recordList) {
        this.mContext = mContext;
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.record_layout, viewGroup, false);
        RecordViewHolder recordViewHolder = new RecordViewHolder(view);
        return recordViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder recordViewHolder, int i) {
        receiptRecord record = recordList.get(i);
        recordViewHolder.textViewAmount.setText("$"+record.getAmount().toString());
        recordViewHolder.textViewName.setText(record.getName());
        recordViewHolder.textViewDate.setText(record.getDate());
        recordViewHolder.textViewCategory.setText(record.getCategory());

        Picasso.get()
                .load(new File(record.getPhotoPath()))
                .centerCrop()
                .fit()
                .into(recordViewHolder.imageViewReceipt);

        //recordViewHolder.imageViewReceipt.setImageURI(Uri.fromFile(new File(record.getPhotoPath())));
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    class RecordViewHolder extends RecyclerView.ViewHolder{

        ImageView imageViewReceipt;
        TextView textViewName, textViewDate, textViewCategory, textViewAmount;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewReceipt = itemView.findViewById(R.id.imageViewReceipt);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewName = itemView.findViewById(R.id.textviewName);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
        }
    }


}
