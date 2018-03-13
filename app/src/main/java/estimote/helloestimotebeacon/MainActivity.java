package estimote.helloestimotebeacon;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.estimote.cloud_plugin.common.EstimoteCloudCredentials;
import com.estimote.internal_plugins_api.cloud.CloudCredentials;
import com.estimote.internal_plugins_api.cloud.proximity.ProximityAttachment;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.proximity.ProximityObserver;
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder;
import com.estimote.proximity_sdk.proximity.ProximityZone;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    private ProximityObserver proximityObserver;

    private Toast toast;
    private Context context;
    private int toastDuration = Toast.LENGTH_SHORT;

    private CharSequence lila8BeaconText = "Say hi to Lila8";
    private CharSequence ouchBlueBeaconText = "Say hi to blueouch";
    private CharSequence mintyEggBeaconText = "Say hi to mintyEgg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        // Estimote Cloud App Credentials
        CloudCredentials cloudCredentials =
                new EstimoteCloudCredentials("pluspunkt-beacon-p42", "73d29862f9abeb31f31279fbcd75a1d5");

        this.proximityObserver =
                new ProximityObserverBuilder(getApplicationContext(), cloudCredentials)
                        .withOnErrorAction(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e("app", "proximity observer error: " + throwable);
                                return null;
                            }
                        })
                        .withBalancedPowerMode()
                        .build();

        // inNearRange() = 1 meter
        // inFarRange() = 5 meter
        // inCustomRange(double) = add custom distance, the nearer the more precise

        // attachment key/value can be set in estimote cloud for each beacon (pw + user sind auf trello)
       ProximityZone ouchBlueZone = this.proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("name", "ouch_blue")
                .inCustomRange(1)
                .withOnEnterAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment proximityAttachment) {
                        Log.d("app", "Hey you're near ouch blue beacon.");
                        toast.makeText(context, ouchBlueBeaconText, toastDuration).show();
                        return null;
                    }
                })
                .withOnExitAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment proximityAttachment) {
                        Log.d("app", "Bye bye, ouch blue beacon will miss you");
                        toast.makeText(context, ouchBlueBeaconText, toastDuration).show();
                        return null;
                    }
                })
                .create();

        this.proximityObserver.addProximityZone(ouchBlueZone);

        ProximityZone lila8ZoneNear = this.proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("name", "lila8")
                .inCustomRange(1)
                .withOnEnterAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment proximityAttachment) {
                        Log.d("app", "Hey you're very close to lila8 beacon. Back off!");
                        toast.makeText(context, lila8BeaconText, toastDuration).show();
                        return null;
                    }
                })
                .create();

        ProximityZone lila8ZoneFar = this.proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("name", "lila8")
                .inCustomRange(10)
                .withOnEnterAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment proximityAttachment) {
                        Log.d("app", "Hey good distance to lila8 beacon.");
                        toast.makeText(context, lila8BeaconText, toastDuration).show();
                        return null;
                    }
                })
                .create();

        this.proximityObserver.addProximityZone(lila8ZoneFar).addProximityZone(lila8ZoneNear);


        ProximityZone beaconZone = this.proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("function", "send_stuff")
                .inNearRange()
                .withOnChangeAction(new Function1<List<? extends ProximityAttachment>, Unit>() {
                    @Override
                    public Unit invoke(List<? extends ProximityAttachment> proximityAttachments) {
                        List<String> beaconNames = new ArrayList<>();
                        for (ProximityAttachment attachment : proximityAttachments){
                            beaconNames.add(attachment.getPayload().get("name"));
                        }
                        Log.d("app", "Nearby beacons: " + beaconNames);
                        toast.makeText(context, "Nearby beacons: " + beaconNames, toastDuration).show();
                        return null;
                    }
                })
                .create();

        this.proximityObserver.addProximityZone(beaconZone);

        // helper class to implement the request location permission from the user
        // other way to do that: https://developer.android.com/training/permissions/requesting.html
        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(this,
                        // onRequirementsFulfilled
                        new Function0<Unit>() {
                            @Override public Unit invoke() {
                                Log.d("app", "requirements fulfilled");
                                proximityObserver.start();
                                return null;
                            }
                        },
                        // onRequirementsMissing
                        new Function1<List<? extends Requirement>, Unit>() {
                            @Override public Unit invoke(List<? extends Requirement> requirements) {
                                Log.e("app", "requirements missing: " + requirements);
                                return null;
                            }
                        },
                        // onError
                        new Function1<Throwable, Unit>() {
                            @Override public Unit invoke(Throwable throwable) {
                                Log.e("app", "requirements error: " + throwable);
                                return null;
                            }
                        });
    }
}
