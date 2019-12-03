package com.via.letmein.ui.administration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.via.letmein.R;
import com.via.letmein.persistence.api.ApiResponse;
import com.via.letmein.persistence.api.pojo.response.HouseholdMember;
import com.via.letmein.ui.main_activity.MainActivity;

import java.util.List;

import static com.via.letmein.persistence.repository.HouseholdMemberRepository.ERROR_DATABASE_ERROR;
import static com.via.letmein.persistence.repository.HouseholdMemberRepository.ERROR_EXPIRED_SESSION_ID;
import static com.via.letmein.persistence.repository.HouseholdMemberRepository.ERROR_MISSING_REQUIRED_PARAMETERS;
import static com.via.letmein.ui.member_profile.MemberProfileFragment.BUNDLE_ID_KEY;
import static com.via.letmein.ui.member_profile.MemberProfileFragment.BUNDLE_NAME_KEY;
import static com.via.letmein.ui.member_profile.MemberProfileFragment.BUNDLE_ROLE_KEY;

/**
 * Fragment housing all the members and allowing closer inspections of individual members.
 *
 * @author Tomas Koristka: 291129@via.dk
 */
public class AdministrationFragment extends Fragment implements MemberAdapter.OnItemClickListener {

    private RecyclerView membersRecyclerView;
    private MemberAdapter membersAdapter;

    private AdministrationViewModel administrationViewModel;
    private FloatingActionButton addMemberButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        administrationViewModel = ViewModelProviders.of(this).get(AdministrationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_administration, container, false);

        initialiseLayout(root);
        observeData();
        return root;
    }

    /**
     * Set an observer to the data provided by {@see AdministrationViewModel#getAllHouseholdMembers} and pass them to the {@see AdministrationFragment#membersAdapter}.
     */
    private void observeData() {
        administrationViewModel.getAllHouseholdMembers("").observe(this, new Observer<ApiResponse>() {
            @Override
            public void onChanged(ApiResponse response) {
                if (response != null) {

                    if (!response.isError() && response.getContent() != null)
                        membersAdapter.setData((List<HouseholdMember>) response.getContent());

                    if (response.isError() && response.getErrorMessage() != null)
                        handleError(response.getErrorMessage());

                }
            }

            private void handleError(String errorMessage) {
                switch (errorMessage) {
                    case ERROR_EXPIRED_SESSION_ID: {
                        ((MainActivity) getActivity()).login();
                    }
                    case ERROR_MISSING_REQUIRED_PARAMETERS: {
                        Toast.makeText(getContext(), "Missing required parameters", Toast.LENGTH_SHORT).show();
                    }
                    case ERROR_DATABASE_ERROR: {
                        Toast.makeText(getContext(), "Database error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * Initialises the fragment's layout
     *
     * @param root Parent view of the fragment.
     */
    private void initialiseLayout(View root) {
        initialiseAdapter();
        initialiseMemberRecyclerView(root);
        initialiseAddMemberButton(root);
    }

    /**
     * Initialises {@see AdministrationFragment#membersAdapter}.
     */
    private void initialiseAdapter() {
        membersAdapter = new MemberAdapter(this);
    }

    /**
     * Initialises the Add member button.
     *
     * @param root Parent view
     */
    private void initialiseAddMemberButton(View root) {
        addMemberButton = root.findViewById(R.id.addMemberButton);
        addMemberButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.nav_add_member);
        });
    }

    /**
     * Initialises the recycler view of the layout.
     *
     * @param root Parent layout.
     */
    private void initialiseMemberRecyclerView(final View root) {
        membersRecyclerView = root.findViewById(R.id.membersRecyclerView);
        membersRecyclerView.hasFixedSize();
        membersRecyclerView.setAdapter(membersAdapter);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false));
        //Swipe to delete item
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; //no move functionality
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                HouseholdMember memberToDelete = membersAdapter.getMemberAt(viewHolder.getAdapterPosition());
                administrationViewModel.delete(memberToDelete);
                Toast.makeText(root.getContext(), "", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(membersRecyclerView);

    }

    /**
     * Opens {@see MemberProfileFragment} of the clicked on item of the list.
     */
    @Override
    public void onItemClick(HouseholdMember item) {

        Bundle extras = new Bundle();

        //TODO consider making Member serialisable and send the Member instance instead?
        extras.putString(BUNDLE_NAME_KEY, item.getName());
        extras.putString(BUNDLE_ROLE_KEY, item.getRole());
        extras.putInt(BUNDLE_ID_KEY, item.getId());
        //extras.putInt(BUNDLE_IMAGEID_KEY, item.getImageID());

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.nav_view_member, extras);
    }
}
