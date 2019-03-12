package com.github.tng.vnv.planner.data.service

import com.github.tng.vnv.planner.model.PackageMetadata

interface CatalogueHelperService {

    Map discoverAssociatedNssAndTests(PackageMetadata packageMetadata)
}