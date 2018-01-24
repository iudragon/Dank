package me.saket.dank.ui.submission;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.saket.dank.R;
import me.saket.dank.data.DankRedditClient;
import me.saket.dank.data.links.RedditCommentLink;
import me.saket.dank.data.links.RedditSubmissionLink;
import me.saket.dank.ui.DankPullCollapsibleActivity;
import me.saket.dank.ui.subreddits.SubmissionPageAnimationOptimizer;
import me.saket.dank.ui.subreddits.SubredditActivity;
import me.saket.dank.utils.DankSubmissionRequest;
import me.saket.dank.utils.Optional;
import me.saket.dank.widgets.InboxUI.IndependentExpandablePageLayout;

/**
 * An Activity that can only show a submission, unlike {@link SubredditActivity} which can shows
 * subreddit submissions as well as holds the submission fragment.
 * <p>
 * This Activity exists because we want to show a submission directly when a comment URL is clicked,
 * where pull-to-collapse will take the user back to the previous screen. This is unlike
 * {@link SubredditActivity}, which takes user back to the submission's subreddit.
 */
public class SubmissionPageLayoutActivity extends DankPullCollapsibleActivity implements SubmissionPageLayout.Callbacks {

  private static final String KEY_SUBMISSION_LINK = "submissionLink";
  private static final String KEY_SUBMISSION_REQUEST = "submission";

  @BindView(R.id.independentsubmission_root) IndependentExpandablePageLayout contentPage;
  @BindView(R.id.independentsubmission_submission_page) SubmissionPageLayout submissionPageLayout;

  /**
   * @param expandFromShape The initial shape from where this Activity will begin its entry expand animation.
   */
  public static void start(Context context, RedditSubmissionLink submissionLink, @Nullable Rect expandFromShape) {
    Intent intent = new Intent(context, SubmissionPageLayoutActivity.class);
    intent.putExtra(KEY_SUBMISSION_LINK, submissionLink);
    intent.putExtra(KEY_EXPAND_FROM_SHAPE, expandFromShape);
    context.startActivity(intent);
  }

  /**
   * @param expandFromShape The initial shape from where this Activity will begin its entry expand animation.
   */
  public static void start(Context context, DankSubmissionRequest submissionRequest, @Nullable Rect expandFromShape) {
    Intent intent = new Intent(context, SubmissionPageLayoutActivity.class);
    intent.putExtra(KEY_SUBMISSION_REQUEST, submissionRequest);
    intent.putExtra(KEY_EXPAND_FROM_SHAPE, expandFromShape);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_submission_fragment);
    ButterKnife.bind(this);

    setupContentExpandablePage(contentPage);
    expandFrom(getIntent().getParcelableExtra(KEY_EXPAND_FROM_SHAPE));

    contentPage.setNestedExpandablePage(submissionPageLayout);
    submissionPageLayout.expandImmediately();

    // SubmissionPageLayout will handle retaining its data on its own when savedInstance != null.
    if (savedInstanceState == null) {
      DankSubmissionRequest submissionRequest;
      if (getIntent().hasExtra(KEY_SUBMISSION_REQUEST)) {
        submissionRequest = getIntent().getParcelableExtra(KEY_SUBMISSION_REQUEST);
      } else if (getIntent().hasExtra(KEY_SUBMISSION_LINK)) {
        submissionRequest = defaultRequest(getIntent().getParcelableExtra(KEY_SUBMISSION_LINK));
      } else {
        throw new AssertionError();
      }
      submissionPageLayout.populateUi(Optional.empty(), submissionRequest);
    }
  }

  private DankSubmissionRequest defaultRequest(RedditSubmissionLink submissionLink) {
    // We don't know the suggested sort yet. Attempt with the default sort and if it's found
    // to be different, then do another load.
    DankSubmissionRequest.Builder submissionReqBuilder = DankSubmissionRequest
        .builder(submissionLink.id())
        .commentSort(DankRedditClient.DEFAULT_COMMENT_SORT);

    RedditCommentLink initialComment = submissionLink.initialComment();
    if (initialComment != null) {
      submissionReqBuilder
          .focusComment(initialComment.id())
          .contextCount(initialComment.contextCount());
    }

    return submissionReqBuilder.build();
  }

  @Override
  public SubmissionPageAnimationOptimizer submissionPageAnimationOptimizer() {
    // Should never get used.
    return new SubmissionPageAnimationOptimizer() {
      @Override
      public boolean isOptimizationPending() {
        throw new AssertionError();
      }
    };
  }

  @Override
  public void onClickSubmissionToolbarUp() {
    finish();
  }
}