package com.hackncs.click;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import in.nashapp.androidsummernote.Summernote;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

import static android.app.Activity.RESULT_OK;


public class CreateNotice extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, CheckBox.OnCheckedChangeListener {

    Spinner spinner2, spinner3, spinner4, spinner5;
    private boolean cb1_value, cb2_value, cb3_value, cb4_value, cb5_value;
    private String vis_for_students, vis_for_others, vis_for_hod, vis_for_management, vis_for_faculty;
    private CheckBox b1, b2, b3, b4, b5;
    private Context context;
    private EditText notice_title;
    private String cDescription, cTitle, category, cSpinselection1, cSpinselection2, cSpinselection3, cSpinselection4, cSpinselection5, coursebranchyear;
    private String TOKEN = "token ";
    private String FIRST_NAME, USER_NAME, GROUP, USER_ID, FACULTY_ID;
    private View view;
    static Summernote summernote;
    Menu menu;
    Uri fileUri;
    private final int PICK_FILE_REQUEST = 1;
    Button createNotice;
    ImageButton click_photo;
    static final int REQUEST_TAKE_PHOTO = 2;
    String mCurrentPhotoPath = null;
    Chip chip;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_createnotice, container, false);

//        menu=MainActivity.menu;
        context = getActivity().getApplicationContext();
        chip = view.findViewById(R.id.chip);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPhotoPath = null;
                fileUri = null;
                chip.setVisibility(View.GONE);
            }
        });
        spinner2 = (Spinner) view.findViewById(R.id.spinner_course);
        spinner3 = (Spinner) view.findViewById(R.id.spinner_branch);
        spinner4 = (Spinner) view.findViewById(R.id.spinner_year);
        spinner5 = (Spinner) view.findViewById(R.id.spinner_section);
        spinner_enable(false);
        createNotice = view.findViewById(R.id.create_notice);
        createNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkDetails()) {
                    createNotice(fileUri);
                }
            }
        });
        summernote = (Summernote) view.findViewById(R.id.summernote);
        Button choose = (Button) view.findViewById(R.id.choose_button);
        choose.setOnClickListener(this);
        Spinner spinner1 = (Spinner) view.findViewById(R.id.spinner_category);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(context,
                R.array.uplaod_category, R.layout.spinner_item);
        adapter1.setDropDownViewResource(R.layout.spinner_item);
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener(this);

        click_photo = view.findViewById(R.id.click_photo);
        click_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dispatchTakePictureIntent();

            }
        });
        //Defining Spinners

        defineSpinner();

        b1 = (CheckBox) view.findViewById(R.id.cb1);
        b2 = (CheckBox) view.findViewById(R.id.cb2);
        b3 = (CheckBox) view.findViewById(R.id.cb3);
        b4 = (CheckBox) view.findViewById(R.id.cb4);
        b5 = (CheckBox) view.findViewById(R.id.cb5);
        b1.setOnCheckedChangeListener(this);
        b2.setOnCheckedChangeListener(this);
        b3.setOnCheckedChangeListener(this);
        b4.setOnCheckedChangeListener(this);
        b5.setOnCheckedChangeListener(this);
        return view;

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        summernote.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {

            Uri uri = null;
            if (intent != null) {
                uri = intent.getData();
                fileUri = uri;
                mCurrentPhotoPath = null;
                File file = FileUtils.getFile(getContext(), fileUri);
                chip.setText(file.getName());
                chip.setVisibility(View.VISIBLE);
            }
        }
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            File file = new File(mCurrentPhotoPath);
            fileUri = null;
            chip.setText(file.getName());
            chip.setVisibility(View.VISIBLE);
        }

    }


    public boolean checkDetails() {

        cb1_value = b1.isChecked();
        cb2_value = b2.isChecked();
        cb3_value = b3.isChecked();
        cb4_value = b4.isChecked();
        cb5_value = b5.isChecked();

        if (cb1_value) {
            int spinner_category_length = cSpinselection1.length();
            int spinner_course_length = cSpinselection2.length();
            int spinner_branch_length = cSpinselection3.length();
            int spinner_year_length = cSpinselection4.length();
            int spinner_section_length = cSpinselection5.length();
            if (spinner_category_length > 0 && spinner_branch_length > 0 && spinner_course_length > 0 && spinner_year_length > 0 && spinner_section_length > 0) {

                return true;
            } else {
                Toast.makeText(context, "Please select the required Details", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else if(cb2_value||cb3_value||cb4_value||cb5_value){
            return true;
        }
        else {
            Toast.makeText(context, "Please check the boxes", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.choose_button) {
            attach();
        }
    }

    public String getTime() {
        Calendar c = Calendar.getInstance();
        String formattedDate = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c.getTime());
//        Log.d("Current time =>", formattedDate);
        return formattedDate;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TextView tvSpinselection = (TextView) view;
        switch (parent.getId()) {
            case R.id.spinner_category:
                cSpinselection1 = tvSpinselection.getText().toString();
//                Log.d("----->", cSpinselection1);

                break;
            case R.id.spinner_course:
                cSpinselection2 = tvSpinselection.getText().toString();
//                Log.d("----->", cSpinselection2);
                defineAdapter3(cSpinselection2);
                break;
            case R.id.spinner_branch:
                cSpinselection3 = tvSpinselection.getText().toString();
//                Log.d("----->", cSpinselection3);
                defineAdapter4(cSpinselection2);
                break;
            case R.id.spinner_year:
                cSpinselection4 = tvSpinselection.getText().toString();
//                Log.d("----->", cSpinselection4);
                defineAdapter5(cSpinselection2, cSpinselection3);
                break;
            case R.id.spinner_section:
                cSpinselection5 = tvSpinselection.getText().toString();
//                Log.d("----->", cSpinselection5);
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void defineSpinner() {

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(context,
                R.array.select_course, R.layout.spinner_item);
        adapter2.setDropDownViewResource(R.layout.spinner_item);
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(this);

    }

    public void defineAdapter3(String course) {
        int resId = R.array.select_branch_Btech;
        if (course.equals("BTech"))
            resId = R.array.select_branch_Btech;
        else if (course.equals("MTech"))
            resId = R.array.select_branch_Mtech;
        else if (course.equals("MCA"))
            resId = R.array.select_branch_MCA;
        else if (course.equals("MBA"))
            resId = R.array.select_branch_MBA;
        else if (course.equals("MTech"))
            resId = R.array.select_branch_Mtech;


        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(context,
                resId, R.layout.spinner_item);
        adapter3.setDropDownViewResource(R.layout.spinner_item);
        spinner3.setAdapter(adapter3);
        spinner3.setOnItemSelectedListener(this);
    }

    public void defineAdapter4(String course) {
        int resId = R.array.select_year_Btech;
        if (course.equals("BTech"))
            resId = R.array.select_year_Btech;
        else if (course.equals("MTech") || course.equals("MBA"))
            resId = R.array.select_year_Others;
        else if (course.equals("MCA"))
            resId = R.array.select_year_MCA;


        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(context,
                resId, R.layout.spinner_item);

        adapter4.setDropDownViewResource(R.layout.spinner_item);
        spinner4.setAdapter(adapter4);
        spinner4.setOnItemSelectedListener(this);

    }

    public void defineAdapter5(String course, String branch) {
        int resId = R.array.select_section;
        if (course.equals("BTech")) {
            if (branch.equals("CSE"))
                resId = R.array.select_section_CSE;
            else if (branch.equals("IT"))
                resId = R.array.select_section_IT;
            else if (branch.equals("ME"))
                resId = R.array.select_section_ME;
            else if (branch.equals("ECE"))
                resId = R.array.select_section_ECE;
            else if (branch.equals("CE"))
                resId = R.array.select_section_CE;
            else if (branch.equals("EE"))
                resId = R.array.select_section_EE;
            else if (branch.equals("IC"))
                resId = R.array.select_section_IC;
        } else if (course.equals("MTech") || course.equals("MBA") || course.equals("MCA"))
            resId = R.array.select_section_Others;


        ArrayAdapter<CharSequence> adapter5 = ArrayAdapter.createFromResource(context,
                resId, R.layout.spinner_item);
        adapter5.setDropDownViewResource(R.layout.spinner_item);
        spinner5.setAdapter(adapter5);
        spinner5.setOnItemSelectedListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (b1.isChecked()) {
            spinner_enable(true);
        } else {
            spinner_enable(false);
        }
        if (isChecked) {
            createNotice.setEnabled(true);
        } else if (!(b1.isChecked() || b2.isChecked() || b3.isChecked() || b4.isChecked() || b5.isChecked())) {
            createNotice.setEnabled(false);
        }
    }

    public void attach() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("*/*");

        startActivityForResult(intent, PICK_FILE_REQUEST);


    }

    private void spinner_enable(boolean set) {
        spinner2.setEnabled(set);
        spinner3.setEnabled(set);
        spinner4.setEnabled(set);
        spinner5.setEnabled(set);
    }


    private void createNotice(Uri fileUri) {

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait..");
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Creating Notice...");
        progressDialog.show();
        notice_title = (EditText) getActivity().findViewById(R.id.etNoticeTitle);
        cTitle = notice_title.getText().toString();
        cDescription = summernote.getText();
        category = cSpinselection1.toLowerCase();


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        TOKEN = "token " + sp.getString("com.hackncs.click.TOKEN", "");
        USER_NAME = sp.getString("com.hackncs.click.USERNAME", "");
        FIRST_NAME = sp.getString("com.hackncs.click.FIRST_NAME", "");
        USER_ID = sp.getString("com.hackncs.click.USER_ID", "");
        FACULTY_ID = sp.getString("com.hackncs.click.PROFILE_ID", "");
        vis_for_students = String.valueOf(cb1_value);
        vis_for_hod = String.valueOf(cb1_value || cb2_value);
        vis_for_faculty = String.valueOf(cb1_value || cb3_value);
        vis_for_management = String.valueOf(cb1_value || cb4_value);
        vis_for_others = String.valueOf(cb1_value || cb5_value);
        coursebranchyear = cSpinselection2 + "-" + cSpinselection3 + "-" + cSpinselection4 + "-" + cSpinselection5;
//        Toast.makeText(context, "CourseBranchYear="+coursebranchyear, Toast.LENGTH_SHORT).show();

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", TOKEN);
        headers.put("username", USER_NAME);

        MultipartBody.Part body = fileUri != null ? prepareFilePart("file_attached", fileUri) : (mCurrentPhotoPath != null ? prepareImagePart("file_attached") : null);
        CreateNoticeService apiService = ApiClient.getClient().create(CreateNoticeService.class);

        NoticeModel noticeModel = new NoticeModel(FACULTY_ID, cTitle, cDescription, null, category, vis_for_students, vis_for_hod, vis_for_faculty, vis_for_management, vis_for_others, coursebranchyear, getTime(), getTime());
        HashMap<String, RequestBody> params = new HashMap<>();
        params.put("faculty", createPartFromString(FACULTY_ID));
        params.put("title", createPartFromString(cTitle));
        params.put("description", createPartFromString(cDescription));
        if (cSpinselection1.toLowerCase().equals("training and placement"))
            cSpinselection1 = "tnp";
        params.put("category", createPartFromString(cSpinselection1.toLowerCase()));
        params.put("visible_for_students", createPartFromString(String.valueOf(vis_for_students)));
        params.put("visible_for_hod", createPartFromString(String.valueOf(vis_for_hod)));
        params.put("visible_for_faculty", createPartFromString(String.valueOf(vis_for_faculty)));
        params.put("visible_for_management", createPartFromString(String.valueOf(vis_for_management)));
        params.put("visible_for_others", createPartFromString(String.valueOf(vis_for_others)));
        params.put("course_branch_year", createPartFromString(coursebranchyear));
        params.put("created", createPartFromString(getTime()));
        params.put("modified", createPartFromString(getTime()));
        Call<JSONObject> call = apiService.upload(headers, params, body);
        call.enqueue(new Callback<JSONObject>() {

            @Override
            public void onResponse(Call<JSONObject> call, retrofit2.Response<JSONObject> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Notice Create Successfully", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();

                } else {
                    Toast.makeText(getContext(), "Please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                progressDialog.dismiss();
//                Log.e("Upload error:", t.toString());
            }
        });

    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {

        File file = FileUtils.getFile(getContext(), fileUri);
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getActivity().getContentResolver().getType(fileUri)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);

    }

    @NonNull
    private MultipartBody.Part prepareImagePart(String partName) {

        File file = new File(mCurrentPhotoPath);
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("image/jpg"),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);

    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    private File createImageFile() throws IOException {
        String timeStamp = getTime();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.i("error", ex.toString());
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

}