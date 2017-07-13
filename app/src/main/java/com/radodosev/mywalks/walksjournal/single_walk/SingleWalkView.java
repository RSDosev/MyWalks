/*
 * Copyright 2016 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.radodosev.mywalks.walksjournal.single_walk;

import com.hannesdorfmann.mosby3.mvp.MvpView;

import io.reactivex.Observable;

/**
 * The HomeView responsible to display a list of {@link }
 *
 * @author Hannes Dorfmann
 */
public interface SingleWalkView extends MvpView {

  /**
   * View intent of loading single walk
   */
  Observable<Long> loadWalkIntent();

  /**
   * Renders the view state
   */
  void render(SingleWalkViewState viewState);
}
