package com.via.letmein.ui.member_profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.via.letmein.R;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MemberProfileFragment extends Fragment implements ImageAdapter.OnItemClickListener {

    public static final String BUNDLE_NAME_KEY = "name";
    public static final String BUNDLE_ROLE_KEY = "role";
    public static final String BUNDLE_IMAGEID_KEY = "imageID";
    public static final String BUNDLE_ID_KEY = "id";

    private ImageView profilePicture;
    private TextView name;
    private TextView role;
    private RecyclerView imageGallery;
    private ImageAdapter imagesAdapter;
    private MemberProfileViewModel memberProfileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        memberProfileViewModel = ViewModelProviders.of(this).get(MemberProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_member_profile, container, false);

        String username = getArguments() != null ? getArguments().getString(BUNDLE_NAME_KEY) : "";

        initialiseLayout(root, getArguments());
        observeData(username);

        return root;
    }

    private void observeData(String username) {
        memberProfileViewModel.getImagePaths(username, "").observe(this, response -> {
            if (response != null) {

                if (!response.isError() && response.getContent() != null) {
                    List<String> dummy = (List<String>) response.getContent();
                    List<ImageContainer> imageContainers = new ArrayList<>();
                    for (String string : dummy)
                        imageContainers.add(new ImageContainer(string));

                    imagesAdapter.setData(imageContainers);
                    //todo fetch images
                }
                if (response.isError() && response.getErrorMessage() != null) {
                    handleErrors(response.getErrorMessage());

                }

            }
        });
    }

    /**
     * Handles server's error messages
     * @param errorMessage Error message to be handled
     */
    private void handleErrors(String errorMessage) {
        switch(errorMessage){

        }
    }

    /**
     * Initialises the fragment's layout
     * @param root
     * @param extras
     */
    private void initialiseLayout(View root, Bundle extras) {
        String username = extras != null ? extras.getString(BUNDLE_NAME_KEY) : "";
        String role = extras != null ? extras.getString(BUNDLE_ROLE_KEY) : "";

        name = root.findViewById(R.id.name);
        this.role = root.findViewById(R.id.action);

        name.setText(username);
        this.role.setText(role);


        profilePicture = root.findViewById(R.id.portrait);

        imageGallery = root.findViewById(R.id.recentEntries);
        imageGallery.setHasFixedSize(true);
        imageGallery.setLayoutManager(new GridLayoutManager(getContext(), 3));
        imagesAdapter = new ImageAdapter(this);
        imageGallery.setAdapter(imagesAdapter);

        profilePicture.setVisibility(View.GONE);
        //profilePicture.setImageResource(extras != null ? extras.getInt(BUNDLE_IMAGEID_KEY) : R.mipmap.profile_icon_placeholder);
    }

    @Override
    public void onItemClick(String item) {

    }
}
